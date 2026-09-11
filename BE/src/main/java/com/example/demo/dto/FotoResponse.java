package com.example.demo.dto;

import java.util.UUID;

public record FotoResponse(
		UUID id,
		String url,
		String nomeOriginale,
		String contentType,
		long peso
) {
}
