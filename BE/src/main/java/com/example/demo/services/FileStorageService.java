package com.example.demo.services;

import com.example.demo.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

	private final Path root;

	public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
		this.root = Path.of(uploadDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(root);
		} catch (IOException e) {
			throw new FileStorageException("Impossibile creare la cartella upload " + root, e);
		}
	}

	/** Salva il file con un nome generato e restituisce il nome usato. */
	public String salva(MultipartFile file, FormatoImmagine formato) {
		String nomeFile = UUID.randomUUID() + formato.getEstensioneSalvataggio();
		Path destinazione = risolvi(nomeFile);
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, destinazione);
			return nomeFile;
		} catch (IOException e) {
			throw new FileStorageException("Errore durante il salvataggio del file", e);
		}
	}

	public void elimina(String nomeFile) {
		try {
			Files.deleteIfExists(risolvi(nomeFile));
		} catch (IOException e) {
			// Il record è già stato rimosso: il file orfano non deve bloccare l'operazione
			log.warn("Impossibile eliminare il file {}", nomeFile, e);
		}
	}

	private Path risolvi(String nomeFile) {
		Path path = root.resolve(nomeFile).normalize();
		if (!path.startsWith(root)) {
			throw new FileStorageException("Percorso file non valido: " + nomeFile, null);
		}
		return path;
	}
}
