package com.example.demo.repositories;

import com.example.demo.entities.Documento;
import com.example.demo.entities.MetodoEstrazione;
import com.example.demo.entities.StatoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {

	List<Documento> findByUtenteIdOrderByCreatedAtDesc(UUID utenteId);

	// Ricerca nel nome e nel testo estratto; pattern già in minuscolo con escape '!'
	@Query("""
			select d from Documento d
			where d.utente.id = :utenteId
			  and (lower(d.nomeOriginale) like :pattern escape '!' or lower(d.testo) like :pattern escape '!')
			order by d.createdAt desc
			""")
	List<Documento> cerca(UUID utenteId, String pattern);

	long countByUtenteId(UUID utenteId);

	List<Documento> findByStatoIn(Collection<StatoDocumento> stati);

	// Aggiornamenti mirati dal thread OCR: se il documento è stato eliminato nel frattempo non modificano nulla
	@Modifying
	@Transactional
	@Query("update Documento d set d.stato = :stato, d.errore = null where d.id = :id")
	int aggiornaStato(UUID id, StatoDocumento stato);

	// Aggiorna sempre il testo OCR originale; il testo corrente solo se l'utente non l'ha corretto
	@Modifying
	@Transactional
	@Query("""
			update Documento d
			set d.stato = :stato, d.metodo = :metodo, d.pagine = :pagine, d.testoOcr = :testo,
			    d.testo = case when d.modificatoAt is null then :testo else d.testo end,
			    d.errore = null, d.elaboratoAt = :elaboratoAt
			where d.id = :id
			""")
	int completa(UUID id, StatoDocumento stato, MetodoEstrazione metodo, int pagine, String testo, Instant elaboratoAt);

	@Modifying
	@Transactional
	@Query("update Documento d set d.stato = :stato, d.errore = :errore, d.elaboratoAt = :elaboratoAt where d.id = :id")
	int fallisci(UUID id, StatoDocumento stato, String errore, Instant elaboratoAt);
}
