package com.example.demo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "foto")
@Getter
@Setter
@NoArgsConstructor
public class Foto {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// Nome generato del file salvato su disco (UUID + estensione), mai il nome originale
	@Column(nullable = false, unique = true)
	private String nomeFile;

	private String nomeOriginale;

	@Column(nullable = false, length = 20)
	private String contentType;

	@Column(nullable = false)
	private long peso;

	@Column(nullable = false)
	private int ordine;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	public Foto(String nomeFile, String nomeOriginale, String contentType, long peso) {
		this.nomeFile = nomeFile;
		this.nomeOriginale = nomeOriginale;
		this.contentType = contentType;
		this.peso = peso;
	}
}
