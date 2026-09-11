package com.example.demo.services;

import java.util.Arrays;
import java.util.Set;

public enum FormatoImmagine {

	JPEG("image/jpeg", ".jpg", Set.of("jpg", "jpeg"), new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
	PNG("image/png", ".png", Set.of("png"), new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

	private final String contentType;
	private final String estensioneSalvataggio;
	private final Set<String> estensioniAmmesse;
	private final byte[] magicBytes;

	FormatoImmagine(String contentType, String estensioneSalvataggio, Set<String> estensioniAmmesse, byte[] magicBytes) {
		this.contentType = contentType;
		this.estensioneSalvataggio = estensioneSalvataggio;
		this.estensioniAmmesse = estensioniAmmesse;
		this.magicBytes = magicBytes;
	}

	public String getContentType() {
		return contentType;
	}

	public String getEstensioneSalvataggio() {
		return estensioneSalvataggio;
	}

	public static FormatoImmagine daEstensione(String estensione) {
		return Arrays.stream(values())
				.filter(f -> f.estensioniAmmesse.contains(estensione))
				.findFirst()
				.orElse(null);
	}

	public static FormatoImmagine daMagicBytes(byte[] header) {
		return Arrays.stream(values())
				.filter(f -> header.length >= f.magicBytes.length
						&& Arrays.equals(header, 0, f.magicBytes.length, f.magicBytes, 0, f.magicBytes.length))
				.findFirst()
				.orElse(null);
	}
}
