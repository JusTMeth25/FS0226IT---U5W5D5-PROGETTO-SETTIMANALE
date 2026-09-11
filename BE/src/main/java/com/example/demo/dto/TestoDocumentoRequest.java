package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// conferma = true: salva il documento nell'archivio (DA_REVISIONARE -> COMPLETATO)
public record TestoDocumentoRequest(
		@NotNull(message = "Il testo è obbligatorio")
		@Size(max = 2_000_000, message = "Il testo è troppo lungo")
		String testo,

		boolean conferma
) {
}
