package com.example.demo.exceptions;

import java.util.UUID;

public class NotFoundException extends RuntimeException {

	public NotFoundException(UUID id) {
		super("Post con id " + id + " non trovato");
	}

	public NotFoundException(String message) {
		super(message);
	}
}
