package com.example.demo.dto;

import java.time.Instant;
import java.util.UUID;

public record UtenteResponse(
		UUID id,
		String nome,
		String email,
		Instant createdAt,
		long documenti
) {
}
