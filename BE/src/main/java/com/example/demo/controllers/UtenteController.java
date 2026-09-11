package com.example.demo.controllers;

import com.example.demo.dto.UtenteRequest;
import com.example.demo.dto.UtenteResponse;
import com.example.demo.services.UtenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/utenti")
@RequiredArgsConstructor
public class UtenteController {

	private final UtenteService utenteService;

	@GetMapping
	public List<UtenteResponse> getAll() {
		return utenteService.elenco();
	}

	@GetMapping("/{id}")
	public UtenteResponse getById(@PathVariable UUID id) {
		return utenteService.dettaglio(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UtenteResponse create(@Valid @RequestBody UtenteRequest req) {
		return utenteService.crea(req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		utenteService.elimina(id);
	}
}
