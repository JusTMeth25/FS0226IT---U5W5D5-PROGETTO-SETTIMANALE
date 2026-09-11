package com.example.demo.dto;

import java.time.Instant;
import java.util.List;

public record ErroreResponse(
		String messaggio,
		List<String> dettagli,
		Instant timestamp
) {
	public ErroreResponse(String messaggio, List<String> dettagli) {
		this(messaggio, dettagli, Instant.now());
	}
}
