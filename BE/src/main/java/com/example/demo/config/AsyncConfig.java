package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	// Pool dedicato all'OCR: pochi thread perché Tesseract usa molta CPU
	@Bean(name = "ocrExecutor")
	public ThreadPoolTaskExecutor ocrExecutor(OcrProperties props) {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(props.thread());
		executor.setMaxPoolSize(props.thread());
		executor.setQueueCapacity(200);
		executor.setThreadNamePrefix("ocr-");
		return executor;
	}
}
