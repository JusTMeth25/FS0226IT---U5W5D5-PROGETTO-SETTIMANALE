package com.example.demo.dto;

import com.example.demo.entities.FontePost;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PostCreateRequest(
		@NotBlank(message = "Il titolo è obbligatorio")
		@Size(max = 100, message = "Il titolo non può superare 100 caratteri")
		String titolo,

		@Size(max = 2000, message = "La descrizione non può superare 2000 caratteri")
		String descrizione,

		@NotNull(message = "La fonte è obbligatoria (CAMERA o UPLOAD)")
		FontePost fonte,

		// Posizione facoltativa
		@DecimalMin(value = "-90", message = "La latitudine deve essere tra -90 e 90")
		@DecimalMax(value = "90", message = "La latitudine deve essere tra -90 e 90")
		BigDecimal latitude,

		@DecimalMin(value = "-180", message = "La longitudine deve essere tra -180 e 180")
		@DecimalMax(value = "180", message = "La longitudine deve essere tra -180 e 180")
		BigDecimal longitude,

		@Size(max = 500, message = "L'indirizzo non può superare 500 caratteri")
		String address
) {

	public boolean haPosizione() {
		return latitude != null && longitude != null;
	}

	@AssertTrue(message = "Latitudine e longitudine vanno indicate insieme; l'indirizzo richiede le coordinate")
	public boolean isPosizioneCoerente() {
		if ((latitude == null) != (longitude == null)) {
			return false;
		}
		return haPosizione() || address == null || address.isBlank();
	}
}
