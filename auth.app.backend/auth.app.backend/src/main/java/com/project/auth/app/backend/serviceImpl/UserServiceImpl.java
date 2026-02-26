package com.project.auth.app.backend.serviceImpl;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.auth.app.backend.dto.UserDto;
import com.project.auth.app.backend.entity.User;
import com.project.auth.app.backend.exception.UserException;
import com.project.auth.app.backend.helpers.UserHelper;
import com.project.auth.app.backend.repository.UserRepository;
import com.project.auth.app.backend.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	private UserRepository userRepository;
	private ModelMapper modelMapper;

	public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
	}

	@Override
	@Transactional
	public UserDto createUser(UserDto userDto) {
		if (userDto.getEmail() == null || userDto.getEmail().isBlank())
			throw new UserException("Empty/Blank Email", HttpStatus.BAD_REQUEST);
		if (userRepository.existsByEmail(userDto.getEmail()))
			throw new UserException("User Already Exists", HttpStatus.ALREADY_REPORTED);

		User user = modelMapper.map(userDto, User.class);
		User savedUser = userRepository.save(user);

		return modelMapper.map(savedUser, UserDto.class);
	}

	@Override
	public UserDto getUserByEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserException("Email Not Found", HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDto.class);
	}

	@Override
	@Transactional
	public UserDto updateUser(UserDto userDto, String userId) {
		User existingUser = userRepository.findById(UserHelper.parseUUID(userId))
				.orElseThrow(() -> new UserException("User Not Found", HttpStatus.NOT_FOUND));

		if (userDto.getName() != null)
			existingUser.setName(userDto.getName());
		if (userDto.getImage() != null)
			existingUser.setImage(userDto.getImage());
		if (userDto.getProvider() != null)
			existingUser.setProvider(userDto.getProvider());
		// TODO : Change Password Logic
		existingUser.setEnable(userDto.isEnable());
		User updatedUser = userRepository.save(existingUser);
		return modelMapper.map(updatedUser, UserDto.class);
	}

	@Override
	public UserDto getUserById(String userId) {
		User user = userRepository.findById(UserHelper.parseUUID(userId))
				.orElseThrow(() -> new UserException("User Not Found", HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDto.class);
	}

	@Override
	public Iterable<UserDto> getAllUsers() {
		return userRepository.findAll().stream().map(user -> modelMapper.map(user, UserDto.class)).toList();
	}

	@Override
	@Transactional
	public void deleteUserById(String userId) {
		User user = userRepository.findById(UserHelper.parseUUID(userId))
				.orElseThrow(() -> new UserException("User Not Found", HttpStatus.NOT_FOUND));
		userRepository.delete(user);

	}

}