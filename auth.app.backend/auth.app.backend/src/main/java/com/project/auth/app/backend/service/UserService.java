package com.project.auth.app.backend.service;

import com.project.auth.app.backend.dto.UserDtoRecord;

public interface UserService {

	UserDtoRecord createUser(UserDtoRecord userDto);

	UserDtoRecord getUserByEmail(String email);

	UserDtoRecord updateUser(UserDtoRecord userDto);

	UserDtoRecord getUserById(String userId);

	Iterable<UserDtoRecord> getAllUsers();

	void deleteUserById(String userId);

}
