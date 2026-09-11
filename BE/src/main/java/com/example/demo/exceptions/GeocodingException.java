package com.example.demo.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GeocodingException extends RuntimeException {

	private final HttpStatus status;

	public GeocodingException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}
}
