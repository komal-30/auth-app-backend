package com.project.auth.app.backend.security;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtSecretGenerator {

	public static void main(String[] args) {
		byte[] key = new byte[64]; // 64 BYTES = 512 bits
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(key);

		String secret = Base64.getEncoder().encodeToString(key);
		System.out.println(secret);
	}

}
