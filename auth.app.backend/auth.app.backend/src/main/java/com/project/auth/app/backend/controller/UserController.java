package com.project.auth.app.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.auth.app.backend.dto.UserDto;
import com.project.auth.app.backend.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// Create User
	@PostMapping
	public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
		UserDto createdUser = userService.createUser(userDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

	}

	// Get User
	@GetMapping
	public ResponseEntity<Iterable<UserDto>> getUser() {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());

	}

	// Get User By Id
	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
		return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(userId));

	}

	// Delete
	@DeleteMapping("/{userId}")
	public void deleteUser(@PathVariable String userId) {
		userService.deleteUserById(userId);

	}

	// Get User By Email
	@PutMapping("/{userId}")
	public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, @PathVariable String userId) {
		return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(userDto, userId));

	}

}
