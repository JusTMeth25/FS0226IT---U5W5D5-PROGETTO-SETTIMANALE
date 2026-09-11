package com.example.demo.dto;

import java.math.BigDecimal;

public record PosizioneResponse(
		BigDecimal latitude,
		BigDecimal longitude,
		String address
) {
}
