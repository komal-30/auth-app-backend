package com.project.auth.app.backend.exception;

import org.springframework.http.HttpStatus;

public class UserException extends RuntimeException {

	private HttpStatus httpStatus;

	public UserException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public HttpStatus getStatus() {
		return httpStatus;
	}

}
