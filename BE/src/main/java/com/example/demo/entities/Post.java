package com.example.demo.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 100)
	private String titolo;

	@Column(columnDefinition = "TEXT")
	private String descrizione;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private FontePost fonte;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("ordine ASC")
	private List<Foto> foto = new ArrayList<>();

	public Post(String titolo, String descrizione, FontePost fonte) {
		this.titolo = titolo;
		this.descrizione = descrizione;
		this.fonte = fonte;
	}

	public void aggiungiFoto(Foto f) {
		f.setPost(this);
		f.setOrdine(foto.size());
		foto.add(f);
	}
}
