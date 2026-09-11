package com.example.demo.controllers;

import com.example.demo.dto.DocumentoResponse;
import com.example.demo.dto.TestoDocumentoRequest;
import com.example.demo.services.DocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DocumentoController {

	private final DocumentoService documentoService;

	// q: ricerca nel nome e nel testo estratto
	@GetMapping("/api/utenti/{utenteId}/documenti")
	public List<DocumentoResponse> getByUtente(@PathVariable UUID utenteId,
			@RequestParam(value = "q", required = false) String q) {
		return documentoService.elenco(utenteId, q);
	}

	// 202: i documenti sono salvati, l'OCR procede in background
	@PostMapping(value = "/api/utenti/{utenteId}/documenti", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public List<DocumentoResponse> upload(@PathVariable UUID utenteId,
			@RequestPart(value = "file", required = false) List<MultipartFile> file) {
		return documentoService.carica(utenteId, file);
	}

	@GetMapping("/api/documenti/{id}")
	public DocumentoResponse getById(@PathVariable UUID id) {
		return documentoService.dettaglio(id);
	}

	@GetMapping("/api/documenti/{id}/file")
	public ResponseEntity<Resource> file(@PathVariable UUID id) {
		DocumentoService.FileDocumento file = documentoService.file(id);
		ContentDisposition disposition = ContentDisposition.inline()
				.filename(file.nomeOriginale() == null ? "documento" : file.nomeOriginale(), StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(file.contentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.body(new FileSystemResource(file.percorso()));
	}

	// Modifica del testo estratto; conferma=true salva il documento nell'archivio
	@PutMapping("/api/documenti/{id}/testo")
	public DocumentoResponse updateTesto(@PathVariable UUID id, @Valid @RequestBody TestoDocumentoRequest req) {
		return documentoService.aggiornaTesto(id, req);
	}

	@PostMapping("/api/documenti/{id}/ripristina")
	public DocumentoResponse ripristina(@PathVariable UUID id) {
		return documentoService.ripristinaTesto(id);
	}

	@PostMapping("/api/documenti/{id}/ocr")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public DocumentoResponse rielabora(@PathVariable UUID id) {
		return documentoService.rielabora(id);
	}

	@DeleteMapping("/api/documenti/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		documentoService.elimina(id);
	}
}
