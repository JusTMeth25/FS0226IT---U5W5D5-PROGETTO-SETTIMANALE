package com.example.demo.services;

import com.example.demo.config.OcrProperties;
import com.example.demo.entities.MetodoEstrazione;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

// Test di integrazione con Tesseract reale: richiede BE/tessdata con ita + eng
@EnabledIf("tessdataPresente")
class OcrServiceTest {

	static boolean tessdataPresente() {
		return Files.exists(Path.of("tessdata", "ita.traineddata")) && Files.exists(Path.of("tessdata", "eng.traineddata"));
	}

	private final OcrService ocr = new OcrService(new OcrProperties("tessdata", "ita+eng", 300, 30, 20, 1));

	@TempDir
	Path tmp;

	private static BufferedImage immagineConTesto(String... righe) {
		BufferedImage img = new BufferedImage(1600, 100 + righe.length * 110, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		g.setColor(Color.BLACK);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 64));
		for (int i = 0; i < righe.length; i++) {
			g.drawString(righe[i], 60, 110 + i * 110);
		}
		g.dispose();
		return img;
	}

	@Test
	void ocrSuImmaginePng() throws Exception {
		Path file = tmp.resolve("scan.png");
		ImageIO.write(immagineConTesto("Camera Oscura", "Documento numero 2026"), "png", file.toFile());

		OcrService.Estrazione estrazione = ocr.estrai(file, FormatoDocumento.PNG);

		assertThat(estrazione.metodo()).isEqualTo(MetodoEstrazione.OCR);
		assertThat(estrazione.testo()).containsIgnoringCase("camera oscura").contains("2026");
	}

	@Test
	void ocrSuTiff() throws Exception {
		Path file = tmp.resolve("scan.tif");
		ImageIO.write(immagineConTesto("Ricevuta di pagamento"), "tiff", file.toFile());

		OcrService.Estrazione estrazione = ocr.estrai(file, FormatoDocumento.TIFF);

		assertThat(estrazione.pagine()).isEqualTo(1);
		assertThat(estrazione.testo()).containsIgnoringCase("ricevuta");
	}

	@Test
	void pdfConTestoNonUsaOcr() throws Exception {
		Path file = tmp.resolve("testo.pdf");
		try (PDDocument pdf = new PDDocument()) {
			PDPage pagina = new PDPage();
			pdf.addPage(pagina);
			try (PDPageContentStream cs = new PDPageContentStream(pdf, pagina)) {
				cs.beginText();
				cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
				cs.newLineAtOffset(72, 700);
				cs.showText("Fattura numero 42 del progetto Camera Oscura");
				cs.endText();
			}
			pdf.save(file.toFile());
		}

		OcrService.Estrazione estrazione = ocr.estrai(file, FormatoDocumento.PDF);

		assertThat(estrazione.metodo()).isEqualTo(MetodoEstrazione.TESTO_PDF);
		assertThat(estrazione.testo()).contains("Fattura numero 42 del progetto Camera Oscura");
	}

	@Test
	void pdfScansionatoUsaOcr() throws Exception {
		Path file = tmp.resolve("scansione.pdf");
		BufferedImage img = immagineConTesto("Camera Oscura", "Verbale di consegna");
		try (PDDocument pdf = new PDDocument()) {
			PDPage pagina = new PDPage();
			pdf.addPage(pagina);
			PDImageXObject immagine = LosslessFactory.createFromImage(pdf, img);
			float larghezza = pagina.getMediaBox().getWidth();
			float altezza = larghezza * img.getHeight() / img.getWidth();
			try (PDPageContentStream cs = new PDPageContentStream(pdf, pagina)) {
				cs.drawImage(immagine, 0, pagina.getMediaBox().getHeight() - altezza, larghezza, altezza);
			}
			pdf.save(file.toFile());
		}

		OcrService.Estrazione estrazione = ocr.estrai(file, FormatoDocumento.PDF);

		assertThat(estrazione.metodo()).isEqualTo(MetodoEstrazione.OCR);
		assertThat(estrazione.testo()).containsIgnoringCase("oscura").containsIgnoringCase("consegna");
	}
}
