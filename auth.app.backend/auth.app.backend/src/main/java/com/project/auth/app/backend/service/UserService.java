package com.project.auth.app.backend.service;

import com.project.auth.app.backend.dto.UserDto;

public interface UserService {

	UserDto createUser(UserDto userDto);

	UserDto getUserByEmail(String email);

	UserDto updateUser(UserDto userDto, String userId);

	Iterable<UserDto> getAllUsers();

	void deleteUserById(String userId);

	UserDto getUserById(String userId);

}
