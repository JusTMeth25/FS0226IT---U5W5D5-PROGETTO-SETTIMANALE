package com.example.demo.controllers;

import com.example.demo.dto.PosizioneRequest;
import com.example.demo.dto.PostCreateRequest;
import com.example.demo.dto.PostPatchRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@GetMapping
	public List<PostResponse> getAll() {
		return postService.trovaTutti();
	}

	@GetMapping("/{id}")
	public PostResponse getById(@PathVariable UUID id) {
		return postService.trovaPerId(id);
	}

	// multipart/form-data: titolo, descrizione, fonte (CAMERA|UPLOAD), foto (1..N)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public PostResponse create(@Valid @ModelAttribute PostCreateRequest req,
			@RequestPart(value = "foto", required = false) List<MultipartFile> foto) {
		return postService.crea(req, foto);
	}

	@PatchMapping("/{id}")
	public PostResponse update(@PathVariable UUID id, @Valid @RequestBody PostPatchRequest req) {
		return postService.aggiorna(id, req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		postService.elimina(id);
	}

	@PutMapping("/{id}/posizione")
	public PostResponse setPosizione(@PathVariable UUID id, @Valid @RequestBody PosizioneRequest req) {
		return postService.impostaPosizione(id, req);
	}

	@DeleteMapping("/{id}/posizione")
	public PostResponse deletePosizione(@PathVariable UUID id) {
		return postService.rimuoviPosizione(id);
	}
}
