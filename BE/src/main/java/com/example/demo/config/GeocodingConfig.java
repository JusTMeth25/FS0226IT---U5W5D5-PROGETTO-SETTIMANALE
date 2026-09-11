package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class GeocodingConfig {

	@Bean
	public RestClient geocodingRestClient(GeocodingProperties props) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(3));
		factory.setReadTimeout(Duration.ofSeconds(5));

		return RestClient.builder()
				.baseUrl(props.baseUrl())
				.requestFactory(factory)
				// La chiave resta sul server: il browser chiama solo /api/geocoding
				.defaultHeader("X-Goog-Api-Key", props.apiKey() == null ? "" : props.apiKey())
				.defaultHeader("X-Goog-FieldMask", "results.location,results.formattedAddress,results.placeId")
				.build();
	}
}
