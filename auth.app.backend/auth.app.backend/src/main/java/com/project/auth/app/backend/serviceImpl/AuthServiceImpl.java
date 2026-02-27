package com.project.auth.app.backend.serviceImpl;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.auth.app.backend.dto.UserDto;
import com.project.auth.app.backend.service.AuthService;
import com.project.auth.app.backend.service.UserService;

@Service
public class AuthServiceImpl implements AuthService {

	private UserService userService;
	private PasswordEncoder passwordEncoder;

	public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;

	}

	@Override
	public UserDto registerUser(UserDto userDto) {

		// logic
		// verify email
		// verify pass
		// verify roles
		// TODO
		userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
		return userService.createUser(userDto);
	}

}
