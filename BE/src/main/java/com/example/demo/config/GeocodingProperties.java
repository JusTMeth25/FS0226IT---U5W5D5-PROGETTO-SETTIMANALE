package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// regionCode e languageCode sono parametri delle richieste a Google, non dati del post
@ConfigurationProperties("app.geocoding")
public record GeocodingProperties(
		String baseUrl,
		String apiKey,
		String regionCode,
		String languageCode,
		int maxResults
) {

	public boolean configurato() {
		return apiKey != null && !apiKey.isBlank();
	}
}
