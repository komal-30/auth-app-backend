package com.project.auth.app.backend.exception;

import java.time.LocalDateTime;

import javax.security.auth.login.CredentialExpiredException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.project.auth.app.backend.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(UserException.class)
	public ResponseEntity<ErrorResponse> handleUserException(UserException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getStatus(), ex.getStatus().getReasonPhrase(),
				ex.getMessage(), request.getDescription(false));
		return new ResponseEntity<>(error, ex.getStatus());
	}

//	@ExceptionHandler(UsernameNotFoundException.class)
//	public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UserException ex, WebRequest request) {
//		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getStatus(), ex.getStatus().getReasonPhrase(),
//				ex.getMessage(), request.getDescription(false));
//		return new ResponseEntity<>(error, ex.getStatus());
//	}

	@ExceptionHandler({ UsernameNotFoundException.class, BadCredentialsException.class,
			CredentialExpiredException.class, DisabledException.class })
	public ResponseEntity<ApiError> handleAuthException(Exception ex, HttpServletRequest request) {
		logger.info("Exception : ", ex.getMessage());
		var apiError = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
		return ResponseEntity.badRequest().body(apiError);

	}
}
