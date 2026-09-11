package com.example.demo.dto;

import java.math.BigDecimal;

public record GeocodingResult(
		BigDecimal latitude,
		BigDecimal longitude,
		String address,
		String placeId
) {
}
