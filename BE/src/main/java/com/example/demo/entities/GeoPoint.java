package com.example.demo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

// Posizione del post: value object immutabile incorporato nella tabella posts
@Embeddable
@Getter
public class GeoPoint {

	public static final int SCALA = 6;

	@Column(name = "latitude", precision = 9, scale = SCALA)
	private BigDecimal latitude;

	@Column(name = "longitude", precision = 9, scale = SCALA)
	private BigDecimal longitude;

	@Column(name = "address", length = 500)
	private String address;

	protected GeoPoint() {
	}

	public GeoPoint(BigDecimal latitude, BigDecimal longitude, String address) {
		this.latitude = Objects.requireNonNull(latitude, "latitude").setScale(SCALA, RoundingMode.HALF_UP);
		this.longitude = Objects.requireNonNull(longitude, "longitude").setScale(SCALA, RoundingMode.HALF_UP);
		this.address = address == null || address.isBlank() ? null : address.trim();
	}
}
