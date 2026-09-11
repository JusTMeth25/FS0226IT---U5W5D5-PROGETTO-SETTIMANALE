package com.example.demo.repositories;

import com.example.demo.entities.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, UUID> {

	List<Utente> findAllByOrderByNomeAsc();

	boolean existsByEmailIgnoreCase(String email);
}
