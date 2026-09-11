package com.example.demo.controllers;

import com.example.demo.dto.GeocodingResult;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.services.GeocodingService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

// Proxy verso Google: il frontend non vede mai la API key.
// Validazione parametri tramite method validation integrata di Spring (HandlerMethodValidationException).
@RestController
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
public class GeocodingController {

	private final GeocodingService geocodingService;

	@GetMapping("/search")
	public List<GeocodingResult> search(
			@RequestParam("indirizzo")
			@NotBlank(message = "Indica un indirizzo da cercare")
			@Size(max = 200, message = "L'indirizzo non può superare 200 caratteri")
			String indirizzo) {
		return geocodingService.cerca(indirizzo);
	}

	@GetMapping("/reverse")
	public GeocodingResult reverse(
			@RequestParam("lat")
			@DecimalMin(value = "-90", message = "lat deve essere tra -90 e 90")
			@DecimalMax(value = "90", message = "lat deve essere tra -90 e 90")
			BigDecimal lat,
			@RequestParam("lng")
			@DecimalMin(value = "-180", message = "lng deve essere tra -180 e 180")
			@DecimalMax(value = "180", message = "lng deve essere tra -180 e 180")
			BigDecimal lng) {
		return geocodingService.reverse(lat, lng)
				.orElseThrow(() -> new NotFoundException("Nessun indirizzo trovato per queste coordinate"));
	}
}
