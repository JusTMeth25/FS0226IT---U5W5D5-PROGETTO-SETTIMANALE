package com.example.demo.exceptions;

import com.example.demo.dto.ErroreResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidFileException.class)
	public ResponseEntity<ErroreResponse> handleInvalidFile(InvalidFileException ex) {
		return risposta(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getDettagli());
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErroreResponse> handleNotFound(NotFoundException ex) {
		return risposta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	public ResponseEntity<ErroreResponse> handleValidation(Exception ex) {
		BindingResult result = ex instanceof MethodArgumentNotValidException m ? m.getBindingResult() : (BindException) ex;
		List<String> dettagli = result.getFieldErrors().stream()
				.map(e -> e.getField() + ": " + (e.isBindingFailure() ? "valore non valido" : e.getDefaultMessage()))
				.toList();
		return risposta(HttpStatus.BAD_REQUEST, "Dati del post non validi", dettagli);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ErroreResponse> handleMethodValidation(HandlerMethodValidationException ex) {
		List<String> dettagli = ex.getAllErrors().stream().map(MessageSourceResolvable::getDefaultMessage).toList();
		return risposta(HttpStatus.BAD_REQUEST, "Parametri non validi", dettagli);
	}

	@ExceptionHandler(GeocodingException.class)
	public ResponseEntity<ErroreResponse> handleGeocoding(GeocodingException ex) {
		return risposta(ex.getStatus(), ex.getMessage(), List.of());
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErroreResponse> handleMaxSize(MaxUploadSizeExceededException ex) {
		return risposta(HttpStatus.CONTENT_TOO_LARGE, "File troppo grande",
				List.of("Ogni foto può pesare al massimo 10MB"));
	}

	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<ErroreResponse> handleMultipart(MultipartException ex) {
		return risposta(HttpStatus.BAD_REQUEST, "Richiesta multipart non valida", List.of());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErroreResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return risposta(HttpStatus.BAD_REQUEST, "Parametro '" + ex.getName() + "' non valido", List.of());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroreResponse> handleNotReadable(HttpMessageNotReadableException ex) {
		return risposta(HttpStatus.BAD_REQUEST, "Corpo della richiesta non valido", List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroreResponse> handleGeneric(Exception ex) {
		// Eccezioni standard di Spring (404 risorsa, 405, 415...) mantengono il loro status
		if (ex instanceof ErrorResponse er) {
			return risposta(er.getStatusCode(), ex.getMessage(), List.of());
		}
		log.error("Errore non gestito", ex);
		return risposta(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno del server", List.of());
	}

	private ResponseEntity<ErroreResponse> risposta(HttpStatusCode status, String messaggio, List<String> dettagli) {
		return ResponseEntity.status(status).body(new ErroreResponse(messaggio, dettagli));
	}
}
