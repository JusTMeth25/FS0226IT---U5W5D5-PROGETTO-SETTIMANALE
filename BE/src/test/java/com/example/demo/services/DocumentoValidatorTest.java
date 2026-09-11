package com.example.demo.services;

import com.example.demo.exceptions.InvalidFileException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoValidatorTest {

	private static final byte[] PDF = "%PDF-1.7\n".getBytes();
	private static final byte[] TIFF_LE = {0x49, 0x49, 0x2A, 0x00, 8, 0, 0, 0};
	private static final byte[] TIFF_BE = {0x4D, 0x4D, 0x00, 0x2A, 0, 0, 0, 8};
	private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};

	private final DocumentoValidator validator = new DocumentoValidator(5);

	private static MultipartFile file(String nome, String mime, byte[] contenuto) {
		return new MockMultipartFile("file", nome, mime, contenuto);
	}

	@Test
	void accettaFormatiAmmessi() {
		List<FormatoDocumento> formati = validator.valida(List.of(
				file("contratto.pdf", "application/pdf", PDF),
				file("scan.tif", "image/tiff", TIFF_LE),
				file("scan2.TIFF", "image/tiff", TIFF_BE),
				file("ricevuta.jpg", "image/jpeg", JPEG)));

		assertThat(formati).containsExactly(FormatoDocumento.PDF, FormatoDocumento.TIFF, FormatoDocumento.TIFF,
				FormatoDocumento.JPEG);
	}

	@Test
	void rifiutaPdfFalsificato() {
		assertThatThrownBy(() -> validator.valida(List.of(file("finto.pdf", "application/pdf", "ciao".getBytes()))))
				.isInstanceOf(InvalidFileException.class)
				.satisfies(e -> assertThat(((InvalidFileException) e).getDettagli())
						.singleElement().asString().contains("non è un file PDF valido"));
	}

	@Test
	void rifiutaFormatoNonAmmesso() {
		assertThatThrownBy(() -> validator.valida(List.of(file("relazione.docx",
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "PK".getBytes()))))
				.isInstanceOf(InvalidFileException.class)
				.satisfies(e -> assertThat(((InvalidFileException) e).getDettagli())
						.singleElement().asString().contains("formato non ammesso"));
	}

	@Test
	void rifiutaMimeIncoerenteEListeNonValide() {
		assertThatThrownBy(() -> validator.valida(List.of(file("a.pdf", "image/png", PDF))))
				.isInstanceOf(InvalidFileException.class);
		assertThatThrownBy(() -> validator.valida(List.of())).isInstanceOf(InvalidFileException.class);
		assertThatThrownBy(() -> validator.valida(Collections.nCopies(6, file("a.pdf", "application/pdf", PDF))))
				.isInstanceOf(InvalidFileException.class);
	}
}
