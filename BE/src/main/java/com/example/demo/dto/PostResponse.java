package com.example.demo.dto;

import com.example.demo.entities.FontePost;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
		UUID id,
		String titolo,
		String descrizione,
		FontePost fonte,
		Instant createdAt,
		Instant updatedAt,
		PosizioneResponse posizione,
		List<FotoResponse> foto
) {
}
