package com.project.auth.app.backend.controller;

import java.time.Instant;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.auth.app.backend.dto.LoginRequestRecordDto;
import com.project.auth.app.backend.dto.TokenResponseRecordDto;
import com.project.auth.app.backend.dto.UserDto;
import com.project.auth.app.backend.entity.RefreshToken;
import com.project.auth.app.backend.entity.User;
import com.project.auth.app.backend.repository.RefreshTokenRepository;
import com.project.auth.app.backend.repository.UserRepository;
import com.project.auth.app.backend.security.JwtService;
import com.project.auth.app.backend.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final ModelMapper modelMapper;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthController(AuthService authService, AuthenticationManager authenticationManager,
			UserRepository userRepository, JwtService jwtService, ModelMapper modelMapper,
			RefreshTokenRepository refreshTokenRepository) {
		this.authService = authService;
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.modelMapper = modelMapper;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	// lOGIN - It Generates the JWT Token after validating use credentials
	@PostMapping("/login")
	public ResponseEntity<TokenResponseRecordDto> loginUser(@RequestBody LoginRequestRecordDto login) {

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
		TokenResponseRecordDto response = TokenResponseRecordDto.createTokenResponseRecordDto(accessToken, refreshToken,
				jwtService.getAccessTtlSeconds(), "Bearer", modelMapper.map(user, UserDto.class));

		return ResponseEntity.ok(response);

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
