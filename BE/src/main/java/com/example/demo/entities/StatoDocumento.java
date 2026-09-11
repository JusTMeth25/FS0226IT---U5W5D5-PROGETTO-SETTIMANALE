package com.example.demo.entities;

public enum StatoDocumento {
	IN_ATTESA,
	IN_ELABORAZIONE,
	// OCR concluso: testo da controllare/correggere prima del salvataggio nell'archivio
	DA_REVISIONARE,
	// Testo confermato dall'utente (resta comunque modificabile)
	COMPLETATO,
	ERRORE
}
