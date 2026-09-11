package com.example.demo.dto;

import com.example.demo.entities.MetodoEstrazione;
import com.example.demo.entities.StatoDocumento;

import java.time.Instant;
import java.util.UUID;

// testo è valorizzato solo nel dettaglio; negli elenchi c'è l'anteprima
public record DocumentoResponse(
		UUID id,
		String nomeOriginale,
		String contentType,
		long peso,
		StatoDocumento stato,
		MetodoEstrazione metodo,
		Integer pagine,
		int caratteri,
		String anteprima,
		String testo,
		String errore,
		Instant createdAt,
		Instant elaboratoAt,
		String urlFile
) {
}
