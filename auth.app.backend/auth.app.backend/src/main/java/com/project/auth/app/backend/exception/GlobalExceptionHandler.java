package com.project.auth.app.backend.exception;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserException.class)
	public ResponseEntity<ErrorResponse> handleUserException(UserException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getStatus(), ex.getStatus().getReasonPhrase(),
				ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(error, ex.getStatus());
	}

}
