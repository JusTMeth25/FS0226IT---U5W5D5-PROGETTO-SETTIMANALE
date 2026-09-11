package com.example.demo.dto;

import com.example.demo.entities.MetodoEstrazione;
import com.example.demo.entities.StatoDocumento;

import java.time.Instant;
import java.util.UUID;

// testo e testoOcr sono valorizzati solo nel dettaglio; negli elenchi c'è l'anteprima
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
		String testoOcr,
		boolean modificato,
		String errore,
		Instant createdAt,
		Instant elaboratoAt,
		Instant modificatoAt,
		String urlFile
) {
}
