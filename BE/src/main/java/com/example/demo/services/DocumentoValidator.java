package com.example.demo.services;

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

// Stesse regole di ImageValidator (estensione + MIME + magic bytes) per i documenti del profilo
@Component
public class DocumentoValidator {

	public static final long MAX_BYTES = 20L * 1024 * 1024;

	private final int maxFiles;

	public DocumentoValidator(@Value("${app.documenti.max-files}") int maxFiles) {
		this.maxFiles = maxFiles;
	}

	public List<FormatoDocumento> valida(List<MultipartFile> files) {
		List<MultipartFile> lista = files == null ? List.of() : files;
		if (lista.isEmpty()) {
			throw new InvalidFileException(List.of("Seleziona almeno un documento"));
		}
		if (lista.size() > maxFiles) {
			throw new InvalidFileException(List.of("Puoi caricare al massimo " + maxFiles + " documenti alla volta"));
		}

		List<String> errori = new ArrayList<>();
		List<FormatoDocumento> formati = new ArrayList<>();
		for (MultipartFile file : lista) {
			formati.add(validaFile(file, errori));
		}
		if (!errori.isEmpty()) {
			throw new InvalidFileException(errori);
		}
		return formati;
	}

	private FormatoDocumento validaFile(MultipartFile file, List<String> errori) {
		String nome = file.getOriginalFilename() == null ? "file senza nome" : file.getOriginalFilename();

		if (file.isEmpty()) {
			errori.add(nome + ": il file è vuoto");
			return null;
		}
		if (file.getSize() > MAX_BYTES) {
			errori.add(nome + ": supera la dimensione massima di 20MB");
			return null;
		}

		String estensione = StringUtils.getFilenameExtension(nome);
		FormatoDocumento formato = estensione == null ? null
				: FormatoDocumento.daEstensione(estensione.toLowerCase(Locale.ROOT));
		if (formato == null) {
			errori.add(nome + ": formato non ammesso (solo PDF, JPEG, PNG, TIFF)");
			return null;
		}

		if (!formato.getContentType().equalsIgnoreCase(file.getContentType())) {
			errori.add(nome + ": tipo MIME '" + file.getContentType() + "' non corrisponde a " + formato.getContentType());
			return null;
		}

		try (InputStream in = file.getInputStream()) {
			if (!formato.corrisponde(in.readNBytes(8))) {
				errori.add(nome + ": il contenuto non è un file " + formato.name() + " valido");
				return null;
			}
		} catch (IOException e) {
			errori.add(nome + ": impossibile leggere il file");
			return null;
		}
		return formato;
	}
}
