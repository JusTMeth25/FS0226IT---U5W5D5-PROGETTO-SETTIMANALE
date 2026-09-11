package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UtenteRequest(
		@NotBlank(message = "Il nome è obbligatorio")
		@Size(max = 60, message = "Il nome non può superare 60 caratteri")
		String nome,

		@NotBlank(message = "L'email è obbligatoria")
		@Email(message = "Email non valida")
		@Size(max = 120, message = "L'email non può superare 120 caratteri")
		String email
) {
}
