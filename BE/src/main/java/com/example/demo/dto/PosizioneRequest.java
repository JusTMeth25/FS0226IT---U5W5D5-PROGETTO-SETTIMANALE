package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PosizioneRequest(
		@NotNull(message = "La latitudine è obbligatoria")
		@DecimalMin(value = "-90", message = "La latitudine deve essere tra -90 e 90")
		@DecimalMax(value = "90", message = "La latitudine deve essere tra -90 e 90")
		BigDecimal latitude,

		@NotNull(message = "La longitudine è obbligatoria")
		@DecimalMin(value = "-180", message = "La longitudine deve essere tra -180 e 180")
		@DecimalMax(value = "180", message = "La longitudine deve essere tra -180 e 180")
		BigDecimal longitude,

		@Size(max = 500, message = "L'indirizzo non può superare 500 caratteri")
		String address
) {
}
