package com.project.auth.app.backend.controller;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.auth.app.backend.dto.LoginRequestRecordDto;
import com.project.auth.app.backend.dto.TokenResponseRecordDto;
import com.project.auth.app.backend.dto.UserDto;
import com.project.auth.app.backend.entity.RefreshToken;
import com.project.auth.app.backend.entity.RefreshTokenRequest;
import com.project.auth.app.backend.entity.User;
import com.project.auth.app.backend.repository.RefreshTokenRepository;
import com.project.auth.app.backend.repository.UserRepository;
import com.project.auth.app.backend.security.CookieService;
import com.project.auth.app.backend.security.JwtService;
import com.project.auth.app.backend.service.AuthService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final ModelMapper modelMapper;
	private final RefreshTokenRepository refreshTokenRepository;
	private final CookieService cookieService;

	public AuthController(AuthService authService, AuthenticationManager authenticationManager,
			UserRepository userRepository, JwtService jwtService, ModelMapper modelMapper,
			RefreshTokenRepository refreshTokenRepository, CookieService cookieService) {
		this.authService = authService;
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.modelMapper = modelMapper;
		this.refreshTokenRepository = refreshTokenRepository;
		this.cookieService = cookieService;
	}

	// lOGIN - It Generates the JWT Token after validating use credentials
	@PostMapping("/login")
	public ResponseEntity<TokenResponseRecordDto> loginUser(@RequestBody LoginRequestRecordDto login,
			HttpServletResponse response) {

		// Authenticate user
		authenticateUser(login);

		// After authentication check if user is enabled;
		User user = userRepository.findByEmail(login.email())
				.orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
		if (!user.isEnable())
			throw new DisabledException("User is Disabled");

		// Refresh Token Logic
		String jti = UUID.randomUUID().toString();
		var refreshTokenObject = RefreshToken.builder().jti(jti).user(user).createdAt(Instant.now())
				.expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())).revoked(false).build();

		refreshTokenRepository.save(refreshTokenObject);

		// If user is enabled, generate the JWT access and refresh Token
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken(user, refreshTokenObject.getJti());
		// Response

		// Use cookie service
		cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
		cookieService.addNoStoreHeaders(response);

		TokenResponseRecordDto tokenResponseRecordDto = TokenResponseRecordDto.createTokenResponseRecordDto(accessToken,
				refreshToken, jwtService.getAccessTtlSeconds(), "Bearer", modelMapper.map(user, UserDto.class));

		return ResponseEntity.ok(tokenResponseRecordDto);

	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponseRecordDto> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
			HttpServletResponse response, HttpServletRequest request) throws InterruptedException {

		// Thread.sleep(5000);

		String refreshToken = readRefreshTokenFromRequest(body, request)
				.orElseThrow(() -> new BadCredentialsException("Refresh token is missing"));

		if (!jwtService.isRefreshToken(refreshToken)) {
			throw new BadCredentialsException("Invalid Refresh Token Type");
		}

		String jti = jwtService.getJti(refreshToken);
		UUID userId = jwtService.getUserId(refreshToken);
		RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti)
				.orElseThrow(() -> new BadCredentialsException("Refresh token not recognized"));

		if (storedRefreshToken.isRevoked()) {
			throw new BadCredentialsException("Refresh token expired or revoked");
		}

		if (storedRefreshToken.getExpiredAt().isBefore(Instant.now())) {
			throw new BadCredentialsException("Refresh token expired");
		}

		if (!storedRefreshToken.getUser().getId().equals(userId)) {
			throw new BadCredentialsException("Refresh token does not belong to this user");
		}

		// refresh token ko rotate:
		storedRefreshToken.setRevoked(true);
		String newJti = UUID.randomUUID().toString();
		storedRefreshToken.setReplaceByToken(newJti);
		refreshTokenRepository.save(storedRefreshToken);

		User user = storedRefreshToken.getUser();

		var newRefreshTokenOb = RefreshToken.builder().jti(newJti).user(user).createdAt(Instant.now())
				.expiredAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())).revoked(false).build();

		refreshTokenRepository.save(newRefreshTokenOb);
		String newAccessToken = jwtService.generateAccessToken(user);
		String newRefreshToken = jwtService.generateRefreshToken(user, newRefreshTokenOb.getJti());

		cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshTtlSeconds());
		cookieService.addNoStoreHeaders(response);
		return ResponseEntity.ok(TokenResponseRecordDto.createTokenResponseRecordDto(newAccessToken, newRefreshToken,
				jwtService.getAccessTtlSeconds(),"", modelMapper.map(user, UserDto.class)));

	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		readRefreshTokenFromRequest(null, request).ifPresent(token -> {
			try {
				if (jwtService.isRefreshToken(token)) {
					String jti = jwtService.getJti(token);
					refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
						rt.setRevoked(true);
						refreshTokenRepository.save(rt);
					});
				}
			} catch (JwtException ignored) {
			}
		});

		// Use CookieUtil (same behavior)
		cookieService.clearRefreshCookie(response);
		cookieService.addNoStoreHeaders(response);
		SecurityContextHolder.clearContext();
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
//      1. prefer reading refresh token from cookie
		if (request.getCookies() != null) {

			Optional<String> fromCookie = Arrays.stream(request.getCookies())
					.filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName())).map(Cookie::getValue)
					.filter(v -> !v.isBlank()).findFirst();

			if (fromCookie.isPresent()) {
				return fromCookie;
			}

		}

		// 2 body:
		if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
			return Optional.of(body.refreshToken());
		}

		// 3. custom header
		String refreshHeader = request.getHeader("X-Refresh-Token");
		if (refreshHeader != null && !refreshHeader.isBlank()) {
			return Optional.of(refreshHeader.trim());
		}

		// Authorization = Bearer <token>
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
			String candidate = authHeader.substring(7).trim();
			if (!candidate.isEmpty()) {
				try {
					if (jwtService.isRefreshToken(candidate)) {
						return Optional.of(candidate);
					}
				} catch (Exception ignored) {
				}
			}
		}

		return Optional.empty();

	}

	// Create User
	@PostMapping("/register")
	public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
		UserDto createdUser = authService.registerUser(userDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

	}

	private Authentication authenticateUser(LoginRequestRecordDto login) {
		try {
			return authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(login.email(), login.password()));
		} catch (Exception e) {
			throw new BadCredentialsException("Invalid username or password");
		}
	}

}
