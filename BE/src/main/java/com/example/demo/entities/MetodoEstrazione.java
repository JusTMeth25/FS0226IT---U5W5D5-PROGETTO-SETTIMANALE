package com.example.demo.entities;

public enum MetodoEstrazione {
	// Testo già presente nel PDF, nessun OCR necessario
	TESTO_PDF,
	// Riconoscimento ottico con Tesseract
	OCR,
	// PDF con pagine di testo e pagine scansionate
	MISTO
}
