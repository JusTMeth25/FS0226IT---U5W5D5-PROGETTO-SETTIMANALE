package com.example.demo.repositories;

import com.example.demo.entities.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

	@EntityGraph(attributePaths = "foto")
	List<Post> findAllByOrderByCreatedAtDesc();

	@EntityGraph(attributePaths = "foto")
	Optional<Post> findWithFotoById(UUID id);
}
