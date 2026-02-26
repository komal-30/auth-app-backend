package com.project.auth.app.backend.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse {

	private LocalDateTime timestamp;
	private HttpStatus status;
	private String error;
	private String message;
	private String path;


}
