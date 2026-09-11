package com.example.demo.services;

import com.example.demo.dto.UtenteRequest;
import com.example.demo.dto.UtenteResponse;
import com.example.demo.entities.Documento;
import com.example.demo.entities.Utente;
import com.example.demo.exceptions.ConflictException;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.repositories.DocumentoRepository;
import com.example.demo.repositories.UtenteRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UtenteService {

	private final UtenteRepository utenteRepository;
	private final DocumentoRepository documentoRepository;
	private final FileStorageService documentiStorage;

	public UtenteService(UtenteRepository utenteRepository, DocumentoRepository documentoRepository,
			@Qualifier("documentiStorage") FileStorageService documentiStorage) {
		this.utenteRepository = utenteRepository;
		this.documentoRepository = documentoRepository;
		this.documentiStorage = documentiStorage;
	}

	@Transactional(readOnly = true)
	public List<UtenteResponse> elenco() {
		return utenteRepository.findAllByOrderByNomeAsc().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public UtenteResponse dettaglio(UUID id) {
		return toResponse(cerca(id));
	}

	@Transactional
	public UtenteResponse crea(UtenteRequest req) {
		String email = req.email().trim().toLowerCase(Locale.ROOT);
		if (utenteRepository.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("Esiste già un profilo con l'email " + email);
		}
		return toResponse(utenteRepository.saveAndFlush(new Utente(req.nome().trim(), email)));
	}

	@Transactional
	public void elimina(UUID id) {
		Utente utente = cerca(id);
		List<String> file = documentoRepository.findByUtenteIdOrderByCreatedAtDesc(id).stream()
				.map(Documento::getNomeFile).toList();
		// cascade: elimina anche i documenti del profilo
		utenteRepository.delete(utente);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				file.forEach(documentiStorage::elimina);
			}
		});
	}

	private Utente cerca(UUID id) {
		return utenteRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Profilo con id " + id + " non trovato"));
	}

	private UtenteResponse toResponse(Utente u) {
		return new UtenteResponse(u.getId(), u.getNome(), u.getEmail(), u.getCreatedAt(),
				documentoRepository.countByUtenteId(u.getId()));
	}
}
