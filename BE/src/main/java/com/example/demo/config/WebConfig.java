package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final String uploadDir;
	private final String allowedOrigin;

	public WebConfig(@Value("${app.upload.dir}") String uploadDir,
			@Value("${app.cors.allowed-origin}") String allowedOrigin) {
		this.uploadDir = uploadDir;
		this.allowedOrigin = allowedOrigin;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(allowedOrigin)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String location = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
		if (!location.endsWith("/")) {
			location += "/";
		}
		registry.addResourceHandler("/uploads/**").addResourceLocations(location);
	}
}
