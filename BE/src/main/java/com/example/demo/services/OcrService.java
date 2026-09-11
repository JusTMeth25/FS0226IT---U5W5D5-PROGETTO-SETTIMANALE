package com.example.demo.services;

import com.example.demo.config.OcrProperties;
import com.example.demo.entities.MetodoEstrazione;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Estrazione testo: PDF con testo nativo via PDFBox, pagine scansionate e immagini via Tesseract (Tess4J)
@Slf4j
@Service
public class OcrService {

	public record Estrazione(String testo, MetodoEstrazione metodo, int pagine) {
	}

	private final OcrProperties props;
	private final Path tessdata;

	public OcrService(OcrProperties props) {
		this.props = props;
		this.tessdata = Path.of(props.tessdataPath()).toAbsolutePath().normalize();
		for (String lingua : props.lingue().split("\\+")) {
			if (!Files.exists(tessdata.resolve(lingua + ".traineddata"))) {
				log.warn("Modello Tesseract '{}' non trovato in {}: l'OCR fallirà", lingua, tessdata);
			}
		}
	}

	public Estrazione estrai(Path file, FormatoDocumento formato) throws IOException, TesseractException {
		return switch (formato) {
			case PDF -> estraiPdf(file);
			case TIFF -> estraiTiff(file);
			case JPEG, PNG -> estraiImmagine(file);
		};
	}

	private Estrazione estraiImmagine(Path file) throws IOException, TesseractException {
		BufferedImage immagine = ImageIO.read(file.toFile());
		if (immagine == null) {
			throw new IOException("Immagine non leggibile");
		}
		return new Estrazione(pulisci(nuovoTesseract().doOCR(immagine)), MetodoEstrazione.OCR, 1);
	}

	private Estrazione estraiTiff(Path file) throws IOException, TesseractException {
		try (ImageInputStream in = ImageIO.createImageInputStream(file.toFile())) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (!readers.hasNext()) {
				throw new IOException("TIFF non leggibile");
			}
			ImageReader reader = readers.next();
			try {
				reader.setInput(in);
				int totale = reader.getNumImages(true);
				int daLeggere = Math.min(totale, props.maxPagine());
				Tesseract tesseract = nuovoTesseract();
				List<String> pagine = new ArrayList<>();
				for (int i = 0; i < daLeggere; i++) {
					pagine.add(tesseract.doOCR(reader.read(i)));
				}
				return new Estrazione(unisci(pagine), MetodoEstrazione.OCR, totale);
			} finally {
				reader.dispose();
			}
		}
	}

	private Estrazione estraiPdf(Path file) throws IOException, TesseractException {
		try (PDDocument pdf = Loader.loadPDF(file.toFile())) {
			int totale = pdf.getNumberOfPages();
			int daLeggere = Math.min(totale, props.maxPagine());
			PDFTextStripper stripper = new PDFTextStripper();
			PDFRenderer renderer = new PDFRenderer(pdf);
			Tesseract tesseract = null;
			boolean testoNativo = false;
			boolean ocr = false;

			List<String> pagine = new ArrayList<>();
			for (int i = 0; i < daLeggere; i++) {
				stripper.setStartPage(i + 1);
				stripper.setEndPage(i + 1);
				String testo = stripper.getText(pdf).strip();
				if (testo.length() >= props.minCaratteriPagina()) {
					pagine.add(testo);
					testoNativo = true;
				} else {
					// Pagina senza testo selezionabile: scansione, la renderizziamo e passiamo a Tesseract
					if (tesseract == null) {
						tesseract = nuovoTesseract();
					}
					BufferedImage immagine = renderer.renderImageWithDPI(i, props.dpi(), ImageType.GRAY);
					pagine.add(tesseract.doOCR(immagine));
					ocr = true;
				}
			}

			MetodoEstrazione metodo = testoNativo && ocr ? MetodoEstrazione.MISTO
					: ocr ? MetodoEstrazione.OCR : MetodoEstrazione.TESTO_PDF;
			return new Estrazione(unisci(pagine), metodo, totale);
		}
	}

	// Tesseract non è thread-safe: un'istanza per documento
	private Tesseract nuovoTesseract() {
		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath(tessdata.toString());
		tesseract.setLanguage(props.lingue());
		tesseract.setOcrEngineMode(1);
		tesseract.setPageSegMode(3);
		tesseract.setVariable("user_defined_dpi", String.valueOf(props.dpi()));
		return tesseract;
	}

	private static String unisci(List<String> pagine) {
		if (pagine.size() == 1) {
			return pulisci(pagine.getFirst());
		}
		return IntStream.range(0, pagine.size())
				.mapToObj(i -> "--- Pagina " + (i + 1) + " ---\n" + pulisci(pagine.get(i)))
				.collect(Collectors.joining("\n\n"));
	}

	private static String pulisci(String testo) {
		if (testo == null) {
			return "";
		}
		return testo.replace("\r\n", "\n")
				.replaceAll("[ \\t]+\\n", "\n")
				.replaceAll("\\n{3,}", "\n\n")
				.strip();
	}
}
