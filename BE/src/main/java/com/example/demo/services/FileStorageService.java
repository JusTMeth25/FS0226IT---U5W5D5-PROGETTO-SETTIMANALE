package com.example.demo.services;

import com.example.demo.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

// Salvataggio file su una cartella; istanze configurate in StorageConfig (foto e documenti)
@Slf4j
public class FileStorageService {

	private final Path root;

	public FileStorageService(String dir) {
		this.root = Path.of(dir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(root);
		} catch (IOException e) {
			throw new FileStorageException("Impossibile creare la cartella " + root, e);
		}
	}

	/** Salva il file con un nome generato (UUID + estensione) e restituisce il nome usato. */
	public String salva(MultipartFile file, String estensione) {
		String nomeFile = UUID.randomUUID() + estensione;
		Path destinazione = percorso(nomeFile);
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, destinazione);
			return nomeFile;
		} catch (IOException e) {
			throw new FileStorageException("Errore durante il salvataggio del file", e);
		}
	}

	public void elimina(String nomeFile) {
		try {
			Files.deleteIfExists(percorso(nomeFile));
		} catch (IOException e) {
			// Il record è già stato rimosso: il file orfano non deve bloccare l'operazione
			log.warn("Impossibile eliminare il file {}", nomeFile, e);
		}
	}

	public Path percorso(String nomeFile) {
		Path path = root.resolve(nomeFile).normalize();
		if (!path.startsWith(root)) {
			throw new FileStorageException("Percorso file non valido: " + nomeFile, null);
		}
		return path;
	}
}
