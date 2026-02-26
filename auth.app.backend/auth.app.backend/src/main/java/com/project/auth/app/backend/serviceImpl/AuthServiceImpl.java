package com.project.auth.app.backend.serviceImpl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project.auth.app.backend.dto.UserDto;
import com.project.auth.app.backend.service.AuthService;
import com.project.auth.app.backend.service.UserService;

@Service
public class AuthServiceImpl implements AuthService {

	private UserService userService;
	private ModelMapper modelMapper;

	public AuthServiceImpl(UserService userService, ModelMapper modelMapper) {
		this.userService = userService;
		this.modelMapper = modelMapper;

	}

	@Override
	public UserDto registerUser(UserDto userDto) {

		// logic
		// verify email
		// verify pass
		// verify roles
		// TODO
		return userService.createUser(userDto);
	}

}
