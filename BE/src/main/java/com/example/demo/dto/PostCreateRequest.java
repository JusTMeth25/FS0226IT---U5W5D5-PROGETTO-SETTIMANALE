package com.example.demo.dto;

import com.example.demo.entities.FontePost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
		@NotBlank(message = "Il titolo è obbligatorio")
		@Size(max = 100, message = "Il titolo non può superare 100 caratteri")
		String titolo,

		@Size(max = 2000, message = "La descrizione non può superare 2000 caratteri")
		String descrizione,

		@NotNull(message = "La fonte è obbligatoria (CAMERA o UPLOAD)")
		FontePost fonte
) {
}
