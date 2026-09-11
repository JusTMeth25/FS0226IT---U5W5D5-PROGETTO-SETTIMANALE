package com.example.demo.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidFileException extends RuntimeException {

	private final List<String> dettagli;

	public InvalidFileException(List<String> dettagli) {
		super("Uno o più file non sono validi");
		this.dettagli = dettagli;
	}
}
