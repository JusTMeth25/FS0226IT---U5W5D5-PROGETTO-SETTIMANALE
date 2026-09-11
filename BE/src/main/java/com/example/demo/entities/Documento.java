package com.example.demo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documenti", indexes = @Index(name = "idx_documenti_utente", columnList = "utente_id"))
@Getter
@Setter
@NoArgsConstructor
public class Documento {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "utente_id", nullable = false)
	private Utente utente;

	// Nome generato del file su disco (UUID + estensione), mai il nome originale
	@Column(nullable = false, unique = true)
	private String nomeFile;

	private String nomeOriginale;

	@Column(nullable = false, length = 40)
	private String contentType;

	@Column(nullable = false)
	private long peso;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatoDocumento stato = StatoDocumento.IN_ATTESA;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private MetodoEstrazione metodo;

	private Integer pagine;

	// Testo corrente: quello estratto, eventualmente corretto dall'utente
	@Column(columnDefinition = "TEXT")
	private String testo;

	// Testo originale letto da PDF/OCR, conservato per il ripristino
	@Column(columnDefinition = "TEXT")
	private String testoOcr;

	// Ultima modifica manuale del testo; null = testo uguale all'originale
	private Instant modificatoAt;

	@Column(length = 500)
	private String errore;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant elaboratoAt;

	public Documento(Utente utente, String nomeFile, String nomeOriginale, String contentType, long peso) {
		this.utente = utente;
		this.nomeFile = nomeFile;
		this.nomeOriginale = nomeOriginale;
		this.contentType = contentType;
		this.peso = peso;
	}
}
