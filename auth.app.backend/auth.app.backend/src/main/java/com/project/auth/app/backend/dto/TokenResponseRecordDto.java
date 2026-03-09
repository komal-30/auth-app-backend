package com.project.auth.app.backend.dto;

public record TokenResponseRecordDto(String accessToken, String refreshToken, long expiresin, String tokenType,
		UserDto user) {

	public static TokenResponseRecordDto createTokenResponseRecordDto(String accessToken, String refreshToken, long expiresin, String tokenType,
			UserDto user) {
		return new TokenResponseRecordDto(accessToken, refreshToken, expiresin, "Bearer", user);
	}

}
