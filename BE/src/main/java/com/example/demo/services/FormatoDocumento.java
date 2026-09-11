package com.example.demo.services;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum FormatoDocumento {

	PDF("application/pdf", ".pdf", Set.of("pdf"), List.of(new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D})),
	JPEG("image/jpeg", ".jpg", Set.of("jpg", "jpeg"), List.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})),
	PNG("image/png", ".png", Set.of("png"), List.of(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})),
	// TIFF little endian (II*\0) o big endian (MM\0*)
	TIFF("image/tiff", ".tif", Set.of("tif", "tiff"), List.of(new byte[]{0x49, 0x49, 0x2A, 0x00}, new byte[]{0x4D, 0x4D, 0x00, 0x2A}));

	private final String contentType;
	private final String estensioneSalvataggio;
	private final Set<String> estensioniAmmesse;
	private final List<byte[]> magicBytes;

	FormatoDocumento(String contentType, String estensioneSalvataggio, Set<String> estensioniAmmesse, List<byte[]> magicBytes) {
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

	public static FormatoDocumento daEstensione(String estensione) {
		return Arrays.stream(values()).filter(f -> f.estensioniAmmesse.contains(estensione)).findFirst().orElse(null);
	}

	public static FormatoDocumento daContentType(String contentType) {
		return Arrays.stream(values()).filter(f -> f.contentType.equals(contentType)).findFirst().orElse(null);
	}

	public boolean corrisponde(byte[] header) {
		return magicBytes.stream().anyMatch(m -> header.length >= m.length
				&& Arrays.equals(header, 0, m.length, m, 0, m.length));
	}
}
