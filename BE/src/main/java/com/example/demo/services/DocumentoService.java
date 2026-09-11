package com.example.demo.services;

import com.example.demo.dto.DocumentoResponse;
import com.example.demo.entities.Documento;
import com.example.demo.entities.StatoDocumento;
import com.example.demo.entities.Utente;
import com.example.demo.exceptions.ConflictException;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.repositories.DocumentoRepository;
import com.example.demo.repositories.UtenteRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentoService {

	private static final int LUNGHEZZA_ANTEPRIMA = 280;

	public record FileDocumento(Path percorso, String contentType, String nomeOriginale) {
	}

	private final DocumentoRepository documentoRepository;
	private final UtenteRepository utenteRepository;
	private final DocumentoValidator documentoValidator;
	private final FileStorageService documentiStorage;
	private final ApplicationEventPublisher publisher;

	public DocumentoService(DocumentoRepository documentoRepository, UtenteRepository utenteRepository,
			DocumentoValidator documentoValidator, @Qualifier("documentiStorage") FileStorageService documentiStorage,
			ApplicationEventPublisher publisher) {
		this.documentoRepository = documentoRepository;
		this.utenteRepository = utenteRepository;
		this.documentoValidator = documentoValidator;
		this.documentiStorage = documentiStorage;
		this.publisher = publisher;
	}

	@Transactional(readOnly = true)
	public List<DocumentoResponse> elenco(UUID utenteId, String ricerca) {
		if (!utenteRepository.existsById(utenteId)) {
			throw new NotFoundException("Profilo con id " + utenteId + " non trovato");
		}
		List<Documento> documenti = ricerca == null || ricerca.isBlank()
				? documentoRepository.findByUtenteIdOrderByCreatedAtDesc(utenteId)
				: documentoRepository.cerca(utenteId, pattern(ricerca));
		return documenti.stream().map(d -> toResponse(d, false)).toList();
	}

	@Transactional
	public List<DocumentoResponse> carica(UUID utenteId, List<MultipartFile> files) {
		Utente utente = utenteRepository.findById(utenteId)
				.orElseThrow(() -> new NotFoundException("Profilo con id " + utenteId + " non trovato"));
		// Validazione completa prima di scrivere su disco
		List<FormatoDocumento> formati = documentoValidator.valida(files);

		List<String> fileSalvati = new ArrayList<>();
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_COMMITTED) {
					fileSalvati.forEach(documentiStorage::elimina);
				}
			}
		});

		List<Documento> salvati = new ArrayList<>();
		for (int i = 0; i < files.size(); i++) {
			MultipartFile file = files.get(i);
			FormatoDocumento formato = formati.get(i);
			String nomeFile = documentiStorage.salva(file, formato.getEstensioneSalvataggio());
			fileSalvati.add(nomeFile);
			Documento documento = documentoRepository.save(
					new Documento(utente, nomeFile, file.getOriginalFilename(), formato.getContentType(), file.getSize()));
			salvati.add(documento);
			// L'OCR parte dopo il commit (TransactionalEventListener)
			publisher.publishEvent(new DocumentoDaElaborare(documento.getId()));
		}
		documentoRepository.flush();
		return salvati.stream().map(d -> toResponse(d, false)).toList();
	}

	@Transactional(readOnly = true)
	public DocumentoResponse dettaglio(UUID id) {
		return toResponse(cerca(id), true);
	}

	@Transactional(readOnly = true)
	public FileDocumento file(UUID id) {
		Documento documento = cerca(id);
		Path percorso = documentiStorage.percorso(documento.getNomeFile());
		if (!Files.exists(percorso)) {
			throw new NotFoundException("File del documento non trovato");
		}
		return new FileDocumento(percorso, documento.getContentType(), documento.getNomeOriginale());
	}

	@Transactional
	public DocumentoResponse rielabora(UUID id) {
		Documento documento = cerca(id);
		if (documento.getStato() == StatoDocumento.IN_ATTESA || documento.getStato() == StatoDocumento.IN_ELABORAZIONE) {
			throw new ConflictException("Il documento è già in elaborazione");
		}
		documento.setStato(StatoDocumento.IN_ATTESA);
		documento.setErrore(null);
		documentoRepository.saveAndFlush(documento);
		publisher.publishEvent(new DocumentoDaElaborare(documento.getId()));
		return toResponse(documento, false);
	}

	@Transactional
	public void elimina(UUID id) {
		Documento documento = cerca(id);
		String nomeFile = documento.getNomeFile();
		documentoRepository.delete(documento);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				documentiStorage.elimina(nomeFile);
			}
		});
	}

	private Documento cerca(UUID id) {
		return documentoRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Documento con id " + id + " non trovato"));
	}

	// Ricerca case-insensitive con escape dei caratteri speciali di LIKE
	private static String pattern(String ricerca) {
		String escaped = ricerca.trim().toLowerCase(Locale.ROOT)
				.replace("!", "!!")
				.replace("%", "!%")
				.replace("_", "!_");
		return "%" + escaped + "%";
	}

	private static DocumentoResponse toResponse(Documento d, boolean conTesto) {
		String testo = d.getTesto();
		String anteprima = null;
		if (testo != null && !testo.isBlank()) {
			String compatto = testo.replaceAll("\\s+", " ").strip();
			anteprima = compatto.length() > LUNGHEZZA_ANTEPRIMA
					? compatto.substring(0, LUNGHEZZA_ANTEPRIMA).strip() + "…"
					: compatto;
		}
		return new DocumentoResponse(d.getId(), d.getNomeOriginale(), d.getContentType(), d.getPeso(), d.getStato(),
				d.getMetodo(), d.getPagine(), testo == null ? 0 : testo.length(), anteprima, conTesto ? testo : null,
				d.getErrore(), d.getCreatedAt(), d.getElaboratoAt(), "/api/documenti/" + d.getId() + "/file");
	}
}
