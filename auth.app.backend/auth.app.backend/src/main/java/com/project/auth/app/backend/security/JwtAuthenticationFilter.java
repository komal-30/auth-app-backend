package com.project.auth.app.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project.auth.app.backend.helpers.UserHelper;
import com.project.auth.app.backend.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private JwtService jwtService;
	private UserRepository userRepository;

	// Validate the user by checking the user ID and verifying JWT Token
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);

			if (!jwtService.isAccessToken(token)) {
				filterChain.doFilter(request, response);
				return;

			}
			try {
				Jws<Claims> claims = jwtService.parse(token);
				String userId = claims.getPayload().getSubject();
				UUID userUuid = UserHelper.parseUUID(userId);

				userRepository.findById(userUuid).ifPresent(user -> {

					// If the user is not enabled
					if (user.isEnable()) {
						// If user is present than check the authorities the user has
						List<GrantedAuthority> authorities = user.getRoles() == null ? List.of()
								: user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
										.collect(Collectors.toList());

						// Set it in Interface
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
								user.getEmail(), null, authorities);

						// Extra Security
//					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

						// Set the security context only if the Authentication is null
						if (SecurityContextHolder.getContext().getAuthentication() == null)
							SecurityContextHolder.getContext().setAuthentication(authentication);
					}

				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		filterChain.doFilter(request, response);
	}

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
		super();
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

}
