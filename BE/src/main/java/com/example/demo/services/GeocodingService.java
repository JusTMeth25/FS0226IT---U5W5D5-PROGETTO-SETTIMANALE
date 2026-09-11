package com.example.demo.services;

import com.example.demo.config.GeocodingProperties;
import com.example.demo.dto.GeocodingResult;
import com.example.demo.entities.GeoPoint;
import com.example.demo.exceptions.GeocodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// Client della Geocoding API v4 di Google (geocode.googleapis.com)
@Slf4j
@Service
public class GeocodingService {

	private final RestClient restClient;
	private final GeocodingProperties props;

	public GeocodingService(RestClient geocodingRestClient, GeocodingProperties props) {
		this.restClient = geocodingRestClient;
		this.props = props;
	}

	/** Da indirizzo a coordinate: restituisce al massimo max-results risultati. */
	public List<GeocodingResult> cerca(String indirizzo) {
		RispostaGoogle risposta = chiama(uri -> uri.path("/v4/geocode/address/{indirizzo}")
				.queryParam("regionCode", props.regionCode())
				.queryParam("languageCode", props.languageCode())
				.build(indirizzo.trim()));
		return risultati(risposta).stream().limit(props.maxResults()).toList();
	}

	/** Da coordinate a indirizzo: primo risultato, se esiste. */
	public Optional<GeocodingResult> reverse(BigDecimal latitude, BigDecimal longitude) {
		RispostaGoogle risposta = chiama(uri -> uri.path("/v4/geocode/location/{lat},{lng}")
				.queryParam("regionCode", props.regionCode())
				.queryParam("languageCode", props.languageCode())
				.build(latitude.toPlainString(), longitude.toPlainString()));
		return risultati(risposta).stream().findFirst();
	}

	private RispostaGoogle chiama(Function<UriBuilder, URI> uri) {
		if (!props.configurato()) {
			throw new GeocodingException(HttpStatus.SERVICE_UNAVAILABLE,
					"Geocoding non configurato: manca GOOGLE_API_KEY in env.properties");
		}
		try {
			return restClient.get().uri(uri).retrieve().body(RispostaGoogle.class);
		} catch (RestClientResponseException e) {
			log.warn("Geocoding Google ha risposto {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new GeocodingException(HttpStatus.BAD_GATEWAY, "Servizio di geocoding non disponibile");
		} catch (ResourceAccessException e) {
			log.warn("Geocoding Google non raggiungibile: {}", e.getMessage());
			throw new GeocodingException(HttpStatus.BAD_GATEWAY, "Servizio di geocoding non raggiungibile");
		}
	}

	private List<GeocodingResult> risultati(RispostaGoogle risposta) {
		if (risposta == null || risposta.results() == null) {
			return List.of();
		}
		return risposta.results().stream()
				.filter(r -> r.location() != null && r.location().latitude() != null && r.location().longitude() != null)
				.map(r -> new GeocodingResult(
						r.location().latitude().setScale(GeoPoint.SCALA, RoundingMode.HALF_UP),
						r.location().longitude().setScale(GeoPoint.SCALA, RoundingMode.HALF_UP),
						r.formattedAddress(),
						r.placeId()))
				.toList();
	}

	// Sottoinsieme della risposta Google (limitato anche dal field mask)
	record RispostaGoogle(List<Risultato> results) {
	}

	record Risultato(Coordinate location, String formattedAddress, String placeId) {
	}

	record Coordinate(BigDecimal latitude, BigDecimal longitude) {
	}
}
