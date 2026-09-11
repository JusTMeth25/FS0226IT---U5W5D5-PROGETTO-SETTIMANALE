package com.example.demo.config;

import com.example.demo.services.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class StorageConfig {

	// Foto dei post: cartella servita pubblicamente su /uploads/**
	@Bean
	@Primary
	public FileStorageService fotoStorage(@Value("${app.upload.dir}") String dir) {
		return new FileStorageService(dir);
	}

	// Documenti del profilo: cartella privata, accessibile solo tramite /api/documenti/{id}/file
	@Bean
	public FileStorageService documentiStorage(@Value("${app.documenti.dir}") String dir) {
		return new FileStorageService(dir);
	}
}
