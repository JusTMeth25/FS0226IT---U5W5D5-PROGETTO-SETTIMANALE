package com.example.demo.services;

import com.example.demo.entities.FontePost;
import com.example.demo.exceptions.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ImageValidator {

	public static final long MAX_BYTES = 10L * 1024 * 1024;

	private final int maxFiles;

	public ImageValidator(@Value("${app.upload.max-files}") int maxFiles) {
		this.maxFiles = maxFiles;
	}

	/**
	 * Valida tutti i file e restituisce il formato reale di ciascuno (stesso ordine).
	 * Raccoglie tutti gli errori prima di lanciare l'eccezione.
	 */
	public List<FormatoImmagine> valida(List<MultipartFile> files, FontePost fonte) {
		List<String> errori = new ArrayList<>();
		List<MultipartFile> lista = files == null ? List.of() : files;

		if (lista.isEmpty()) {
			throw new InvalidFileException(List.of("È necessario allegare almeno una foto"));
		}
		if (fonte == FontePost.CAMERA && lista.size() != 1) {
			throw new InvalidFileException(List.of("Con la fotocamera è consentita una sola foto"));
		}
		if (lista.size() > maxFiles) {
			throw new InvalidFileException(List.of("Puoi allegare al massimo " + maxFiles + " foto"));
		}

		List<FormatoImmagine> formati = new ArrayList<>();
		for (MultipartFile file : lista) {
			FormatoImmagine formato = validaFile(file, errori);
			formati.add(formato);
		}

		if (!errori.isEmpty()) {
			throw new InvalidFileException(errori);
		}
		return formati;
	}

	private FormatoImmagine validaFile(MultipartFile file, List<String> errori) {
		String nome = file.getOriginalFilename() == null ? "file senza nome" : file.getOriginalFilename();

		if (file.isEmpty()) {
			errori.add(nome + ": il file è vuoto");
			return null;
		}
		if (file.getSize() > MAX_BYTES) {
			errori.add(nome + ": supera la dimensione massima di 10MB");
			return null;
		}

		String estensione = StringUtils.getFilenameExtension(nome);
		FormatoImmagine daEstensione = estensione == null ? null
				: FormatoImmagine.daEstensione(estensione.toLowerCase(Locale.ROOT));
		if (daEstensione == null) {
			errori.add(nome + ": estensione non ammessa (solo .jpg, .jpeg, .png)");
			return null;
		}

		if (!daEstensione.getContentType().equalsIgnoreCase(file.getContentType())) {
			errori.add(nome + ": tipo MIME '" + file.getContentType() + "' non corrisponde a " + daEstensione.getContentType());
			return null;
		}

		FormatoImmagine reale;
		try (InputStream in = file.getInputStream()) {
			reale = FormatoImmagine.daMagicBytes(in.readNBytes(8));
		} catch (IOException e) {
			errori.add(nome + ": impossibile leggere il file");
			return null;
		}
		if (reale != daEstensione) {
			errori.add(nome + ": il contenuto non è un'immagine " + daEstensione.name() + " valida");
			return null;
		}
		return reale;
	}
}
