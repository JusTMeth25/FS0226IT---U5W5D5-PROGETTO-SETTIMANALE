package com.example.demo.services;

import com.example.demo.dto.FotoResponse;
import com.example.demo.dto.PostCreateRequest;
import com.example.demo.dto.PostPatchRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.entities.Foto;
import com.example.demo.entities.Post;
import com.example.demo.exceptions.NotFoundException;
import com.example.demo.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final FileStorageService fileStorageService;
	private final ImageValidator imageValidator;

	@Transactional(readOnly = true)
	public List<PostResponse> trovaTutti() {
		return postRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public PostResponse trovaPerId(UUID id) {
		return toResponse(cerca(id));
	}

	@Transactional
	public PostResponse crea(PostCreateRequest req, List<MultipartFile> files) {
		// Validazione completa prima di scrivere qualsiasi file su disco
		List<FormatoImmagine> formati = imageValidator.valida(files, req.fonte());

		List<String> fileSalvati = new ArrayList<>();
		// Se la transazione fallisce (anche al commit) i file già scritti vengono rimossi
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_COMMITTED) {
					fileSalvati.forEach(fileStorageService::elimina);
				}
			}
		});

		Post post = new Post(req.titolo().trim(), normalizza(req.descrizione()), req.fonte());
		for (int i = 0; i < files.size(); i++) {
			MultipartFile file = files.get(i);
			FormatoImmagine formato = formati.get(i);
			String nomeFile = fileStorageService.salva(file, formato);
			fileSalvati.add(nomeFile);
			post.aggiungiFoto(new Foto(nomeFile, file.getOriginalFilename(), formato.getContentType(), file.getSize()));
		}

		return toResponse(postRepository.saveAndFlush(post));
	}

	@Transactional
	public PostResponse aggiorna(UUID id, PostPatchRequest req) {
		Post post = cerca(id);
		if (req.titolo() != null) {
			post.setTitolo(req.titolo().trim());
		}
		if (req.descrizione() != null) {
			post.setDescrizione(normalizza(req.descrizione()));
		}
		return toResponse(postRepository.saveAndFlush(post));
	}

	@Transactional
	public void elimina(UUID id) {
		Post post = cerca(id);
		List<String> daEliminare = post.getFoto().stream().map(Foto::getNomeFile).toList();
		postRepository.delete(post);
		// I file vengono cancellati solo se la cancellazione del record va a buon fine
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				daEliminare.forEach(fileStorageService::elimina);
			}
		});
	}

	private Post cerca(UUID id) {
		return postRepository.findWithFotoById(id).orElseThrow(() -> new NotFoundException(id));
	}

	private String normalizza(String descrizione) {
		return descrizione == null || descrizione.isBlank() ? null : descrizione.trim();
	}

	private PostResponse toResponse(Post post) {
		List<FotoResponse> foto = post.getFoto().stream()
				.map(f -> new FotoResponse(f.getId(), "/uploads/" + f.getNomeFile(), f.getNomeOriginale(), f.getContentType(), f.getPeso()))
				.toList();
		return new PostResponse(post.getId(), post.getTitolo(), post.getDescrizione(), post.getFonte(),
				post.getCreatedAt(), post.getUpdatedAt(), foto);
	}
}
