package com.example.demo.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GeoPointTest {

	@Test
	void arrotondaASeiDecimaliENormalizzaIndirizzo() {
		GeoPoint p = new GeoPoint(new BigDecimal("41.8902101999"), new BigDecimal("12.49223095"), "  ");

		assertThat(p.getLatitude()).hasToString("41.890210");
		assertThat(p.getLongitude()).hasToString("12.492231");
		assertThat(p.getAddress()).isNull();
	}

	@Test
	void mantieneEstremi() {
		GeoPoint p = new GeoPoint(new BigDecimal("-90"), new BigDecimal("180"), " Polo ");

		assertThat(p.getLatitude()).hasToString("-90.000000");
		assertThat(p.getLongitude()).hasToString("180.000000");
		assertThat(p.getAddress()).isEqualTo("Polo");
	}
}
