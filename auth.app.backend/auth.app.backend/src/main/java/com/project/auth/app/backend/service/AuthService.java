package com.project.auth.app.backend.service;

import com.project.auth.app.backend.dto.UserDto;

public interface AuthService {

	UserDto registerUser(UserDto userDto);

}
