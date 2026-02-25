package com.project.auth.app.backend.serviceImpl;

import org.springframework.stereotype.Service;

import com.project.auth.app.backend.dto.UserDtoRecord;
import com.project.auth.app.backend.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Override
	public UserDtoRecord createUser(UserDtoRecord userDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserDtoRecord getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserDtoRecord updateUser(UserDtoRecord userDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserDtoRecord getUserById(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<UserDtoRecord> getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteUserById(String userId) {
		// TODO Auto-generated method stub

	}

}
