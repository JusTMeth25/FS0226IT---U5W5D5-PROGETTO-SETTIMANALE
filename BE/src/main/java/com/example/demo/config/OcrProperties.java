package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ocr")
public record OcrProperties(
		String tessdataPath,
		String lingue,
		int dpi,
		int maxPagine,
		int minCaratteriPagina,
		int thread
) {
}
