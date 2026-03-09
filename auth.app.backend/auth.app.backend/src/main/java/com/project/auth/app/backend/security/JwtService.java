package com.project.auth.app.backend.security;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.auth.app.backend.entity.Role;
import com.project.auth.app.backend.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

@Service
@Getter
public class JwtService {

	// Key used to signing and verifying tokens
	private final SecretKey key;
	private final long accessTtlSeconds;
	private final long refreshTtlSeconds;
	private final String issuer;

	public JwtService(@Value("${security.jwt.secret}") String secret,
			@Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
			@Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
			@Value("${security.jwt.issuer}") String issuer) {

		if (secret == null || secret.length() < 64)
			throw new IllegalArgumentException("Invalid Key");

		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.accessTtlSeconds = accessTtlSeconds;
		this.refreshTtlSeconds = refreshTtlSeconds;
		this.issuer = issuer;

	}
	// Access Token - Short Lived
	// Refresh Token - Long Lived, generates access token after expiry without
	// relogging user
	// Claims - Payload inside JWT - subject,expiry,issuer,roles,etc
	// key - Secret key to verify JWT Token
	// JTI - A unique identifier for the JWT. Useful to revoke tokens or prevent
	// replay attacks.

	// Generate new JWT Access Token
	public String generateAccessToken(User user) {
		// Access token should contain - JWT ID, Subject,Issuer,Issued
		// At,Expiration,Claims,Signature
		Instant now = Instant.now();
		List<String> roles = user.getRoles() == null ? List.of()
				: user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
		return Jwts.builder().id(UUID.randomUUID().toString()).subject(user.getId().toString()).issuer(issuer)
				.issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
				.claims(Map.of("email", user.getEmail(), "roles", roles, "typ", "access"))
				.signWith(key,Jwts.SIG.HS512)
				.compact();
	}

	public String generateRefreshToken(User user, String jti) {
		// Refresh token should contain - JWT ID, Subject,Issuer,Issued
		// At,Expiration,Claims,Signature
		Instant now = Instant.now();
		return Jwts.builder().id(jti).subject(user.getId().toString()).issuer(issuer).issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(refreshTtlSeconds))).claims(Map.of("typ", "refresh"))
//		.signWith(key,SignatureAlgorithm.HS512)
				.signWith(key,Jwts.SIG.HS512)
				.compact();

	}

	// Verifies JWT token with key,checks expiry of token
	public Jws<Claims> parse(String token) {
		try {
			return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
		} catch (JwtException ex) {
			throw ex;
		}
	}

	public boolean isAccessToken(String token) {
		Claims c = parse(token).getPayload();
		return "access".equals(c.get("typ"));
	}

	public boolean isRefreshToken(String token) {
		Claims c = parse(token).getPayload();
		return "refresh".equals(c.get("typ"));
	}

	public UUID getUserId(String token) {
		Claims c = parse(token).getPayload();
		return UUID.fromString(c.getSubject());
	}

	public String getJti(String token) {
		return parse(token).getPayload().getId();
	}



}
