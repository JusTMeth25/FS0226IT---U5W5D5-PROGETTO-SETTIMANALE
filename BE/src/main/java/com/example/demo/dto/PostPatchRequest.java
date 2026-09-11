package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Campi opzionali: vengono aggiornati solo quelli presenti (non null)
public record PostPatchRequest(
		@Size(max = 100, message = "Il titolo non può superare 100 caratteri")
		@Pattern(regexp = "(?s).*\\S.*", message = "Il titolo non può essere vuoto")
		String titolo,

		@Size(max = 2000, message = "La descrizione non può superare 2000 caratteri")
		String descrizione
) {
}
