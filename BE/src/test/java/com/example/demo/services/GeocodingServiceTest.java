package com.example.demo.services;

import com.example.demo.config.GeocodingProperties;
import com.example.demo.dto.GeocodingResult;
import com.example.demo.exceptions.GeocodingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeocodingServiceTest {

	private static final String COLOSSEO = """
			{"results":[{"placeId":"ChIJrRMgU7ZhLxMRxAOFkC7I8Sg",
			"location":{"latitude":41.890210199999996,"longitude":12.4922309},
			"formattedAddress":"Piazza del Colosseo, 00184 Roma RM, Italia"}]}
			""";

	private MockRestServiceServer server;

	private GeocodingService service(String apiKey) {
		GeocodingProperties props = new GeocodingProperties("https://geo.test", apiKey, "IT", "it", 5);
		RestClient.Builder builder = RestClient.builder()
				.baseUrl(props.baseUrl())
				.defaultHeader("X-Goog-Api-Key", apiKey);
		server = MockRestServiceServer.bindTo(builder).build();
		return new GeocodingService(builder.build(), props);
	}

	@Test
	void cercaIndirizzo() {
		GeocodingService service = service("chiave-test");
		server.expect(requestTo(containsString("/v4/geocode/address/Colosseo%20Roma")))
				.andExpect(method(org.springframework.http.HttpMethod.GET))
				.andExpect(header("X-Goog-Api-Key", "chiave-test"))
				.andExpect(queryParam("regionCode", "IT"))
				.andExpect(queryParam("languageCode", "it"))
				.andRespond(withSuccess(COLOSSEO, MediaType.APPLICATION_JSON));

		List<GeocodingResult> risultati = service.cerca(" Colosseo Roma ");

		server.verify();
		assertThat(risultati).singleElement().satisfies(r -> {
			assertThat(r.latitude()).isEqualByComparingTo("41.890210");
			assertThat(r.latitude().scale()).isEqualTo(6);
			assertThat(r.longitude()).isEqualByComparingTo("12.492231");
			assertThat(r.address()).isEqualTo("Piazza del Colosseo, 00184 Roma RM, Italia");
		});
	}

	@Test
	void reverseGeocoding() {
		GeocodingService service = service("chiave-test");
		server.expect(requestTo(containsString("/v4/geocode/location/41.890210,12.492231")))
				.andRespond(withSuccess(COLOSSEO, MediaType.APPLICATION_JSON));

		Optional<GeocodingResult> risultato = service.reverse(new BigDecimal("41.890210"), new BigDecimal("12.492231"));

		assertThat(risultato).map(GeocodingResult::address).contains("Piazza del Colosseo, 00184 Roma RM, Italia");
	}

	@Test
	void nessunRisultato() {
		GeocodingService service = service("chiave-test");
		server.expect(requestTo(containsString("/address/"))).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
		server.expect(requestTo(containsString("/location/"))).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		assertThat(service.cerca("xyzxyz")).isEmpty();
		assertThat(service.reverse(BigDecimal.ZERO, BigDecimal.ZERO)).isEmpty();
	}

	@Test
	void erroreGoogleDiventaBadGateway() {
		GeocodingService service = service("chiave-errata");
		server.expect(requestTo(containsString("/address/")))
				.andRespond(withStatus(HttpStatus.FORBIDDEN).body("{\"error\":{\"status\":\"PERMISSION_DENIED\"}}"));

		assertThatThrownBy(() -> service.cerca("Roma"))
				.isInstanceOf(GeocodingException.class)
				.extracting("status").isEqualTo(HttpStatus.BAD_GATEWAY);
	}

	@Test
	void chiaveMancanteNonChiamaGoogle() {
		GeocodingService service = service("");

		assertThatThrownBy(() -> service.cerca("Roma"))
				.isInstanceOf(GeocodingException.class)
				.extracting("status").isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		server.verify();
	}
}
