package com.example.demo.services;

import com.example.demo.entities.FontePost;
import com.example.demo.exceptions.InvalidFileException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageValidatorTest {

	private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F'};
	private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};

	private final ImageValidator validator = new ImageValidator(5);

	private static MultipartFile file(String nome, String mime, byte[] contenuto) {
		return new MockMultipartFile("foto", nome, mime, contenuto);
	}

	@Test
	void accettaJpegEPng() {
		List<FormatoImmagine> formati = validator.valida(List.of(
				file("a.jpg", "image/jpeg", JPEG),
				file("b.JPEG", "image/jpeg", JPEG),
				file("c.png", "image/png", PNG)), FontePost.UPLOAD);

		assertThat(formati).containsExactly(FormatoImmagine.JPEG, FormatoImmagine.JPEG, FormatoImmagine.PNG);
	}

	@Test
	void rifiutaContenutoFalsificato() {
		MultipartFile finto = file("finto.jpg", "image/jpeg", "non sono una foto".getBytes());

		assertThatThrownBy(() -> validator.valida(List.of(finto), FontePost.UPLOAD))
				.isInstanceOf(InvalidFileException.class)
				.satisfies(e -> assertThat(((InvalidFileException) e).getDettagli())
						.singleElement().asString().contains("non è un'immagine JPEG valida"));
	}

	@Test
	void rifiutaPngRinominatoJpg() {
		assertThatThrownBy(() -> validator.valida(List.of(file("x.jpg", "image/jpeg", PNG)), FontePost.UPLOAD))
				.isInstanceOf(InvalidFileException.class);
	}

	@Test
	void rifiutaEstensioneNonAmmessa() {
		assertThatThrownBy(() -> validator.valida(List.of(file("anim.gif", "image/gif", JPEG)), FontePost.UPLOAD))
				.isInstanceOf(InvalidFileException.class)
				.satisfies(e -> assertThat(((InvalidFileException) e).getDettagli())
						.singleElement().asString().contains("estensione non ammessa"));
	}

	@Test
	void rifiutaMimeIncoerente() {
		assertThatThrownBy(() -> validator.valida(List.of(file("a.jpg", "text/plain", JPEG)), FontePost.UPLOAD))
				.isInstanceOf(InvalidFileException.class);
	}

	@Test
	void rifiutaListaVuotaETroppiFile() {
		assertThatThrownBy(() -> validator.valida(List.of(), FontePost.UPLOAD))
				.isInstanceOf(InvalidFileException.class);
		assertThatThrownBy(() -> validator.valida(null, FontePost.UPLOAD))
				.isInstanceOf(InvalidFileException.class);

		List<MultipartFile> sei = Collections.nCopies(6, file("a.jpg", "image/jpeg", JPEG));
		assertThatThrownBy(() -> validator.valida(sei, FontePost.UPLOAD))
				.isInstanceOf(InvalidFileException.class);
	}

	@Test
	void cameraAccettaUnaSolaFoto() {
		MultipartFile f = file("camera.jpg", "image/jpeg", JPEG);

		assertThat(validator.valida(List.of(f), FontePost.CAMERA)).containsExactly(FormatoImmagine.JPEG);
		assertThatThrownBy(() -> validator.valida(List.of(f, f), FontePost.CAMERA))
				.isInstanceOf(InvalidFileException.class);
	}
}
