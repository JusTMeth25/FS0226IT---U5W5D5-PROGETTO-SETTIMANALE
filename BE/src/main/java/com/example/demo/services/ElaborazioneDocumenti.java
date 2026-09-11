package com.example.demo.services;

import com.example.demo.entities.Documento;
import com.example.demo.entities.StatoDocumento;
import com.example.demo.repositories.DocumentoRepository;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.List;

// Elaborazione OCR in background: il caricamento risponde subito, il testo arriva dopo
@Slf4j
@Component
public class ElaborazioneDocumenti {

	private final DocumentoRepository documentoRepository;
	private final OcrService ocrService;
	private final FileStorageService documentiStorage;
	private final ApplicationEventPublisher publisher;

	public ElaborazioneDocumenti(DocumentoRepository documentoRepository, OcrService ocrService,
			@Qualifier("documentiStorage") FileStorageService documentiStorage, ApplicationEventPublisher publisher) {
		this.documentoRepository = documentoRepository;
		this.ocrService = ocrService;
		this.documentiStorage = documentiStorage;
		this.publisher = publisher;
	}

	// Parte solo dopo il commit, così il thread OCR trova il documento sul DB
	@Async("ocrExecutor")
	@TransactionalEventListener(fallbackExecution = true)
	public void elabora(DocumentoDaElaborare evento) {
		Documento documento = documentoRepository.findById(evento.id()).orElse(null);
		if (documento == null) {
			return; // eliminato prima dell'elaborazione
		}

		FormatoDocumento formato = FormatoDocumento.daContentType(documento.getContentType());
		documentoRepository.aggiornaStato(documento.getId(), StatoDocumento.IN_ELABORAZIONE);
		long inizio = System.currentTimeMillis();
		try {
			OcrService.Estrazione estrazione = ocrService.estrai(documentiStorage.percorso(documento.getNomeFile()), formato);
			documentoRepository.completa(documento.getId(), StatoDocumento.COMPLETATO, estrazione.metodo(),
					estrazione.pagine(), estrazione.testo(), Instant.now());
			log.info("Documento {} elaborato: {} pagine, {} caratteri, metodo {}, {} ms", documento.getId(),
					estrazione.pagine(), estrazione.testo().length(), estrazione.metodo(), System.currentTimeMillis() - inizio);
		} catch (Exception | LinkageError e) {
			log.warn("Elaborazione fallita per il documento {}", documento.getId(), e);
			documentoRepository.fallisci(documento.getId(), StatoDocumento.ERRORE, messaggio(e), Instant.now());
		}
	}

	// Documenti rimasti in coda se l'applicazione è stata fermata durante l'elaborazione
	@EventListener(ApplicationReadyEvent.class)
	public void riprendiInSospeso() {
		List<Documento> sospesi = documentoRepository.findByStatoIn(
				List.of(StatoDocumento.IN_ATTESA, StatoDocumento.IN_ELABORAZIONE));
		if (!sospesi.isEmpty()) {
			log.info("Riprendo {} documenti in sospeso", sospesi.size());
			sospesi.forEach(d -> publisher.publishEvent(new DocumentoDaElaborare(d.getId())));
		}
	}

	private static String messaggio(Throwable e) {
		String messaggio = switch (e) {
			case InvalidPasswordException ignored -> "PDF protetto da password";
			case TesseractException t -> "Errore OCR: " + t.getMessage();
			case LinkageError l -> "Libreria OCR non disponibile: " + l.getMessage();
			default -> "Impossibile leggere il documento: " + e.getMessage();
		};
		return messaggio.length() > 500 ? messaggio.substring(0, 500) : messaggio;
	}
}
