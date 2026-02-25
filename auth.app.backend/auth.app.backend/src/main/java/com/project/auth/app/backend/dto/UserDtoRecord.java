package com.project.auth.app.backend.dto;

import java.time.Instant;
import java.util.Set;

import com.project.auth.app.backend.entity.Provider;
import com.project.auth.app.backend.entity.Role;


public record UserDtoRecord(Long id, String email, String name, String password, String image, boolean enable,
		Instant createdAt, Instant updatedAt, Provider provider, Set<Role> roles) {

	// To set the defualt value of enable create a constructor and then pass true;
	public UserDtoRecord(Long id, String email, String name, String password, String image, boolean enable,
			Instant createdAt, Instant updatedAt, Provider provider, Set<Role> roles) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.password = password;
		this.image = image;
		this.enable = true;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.provider = provider;
		this.roles = roles;
	}

}
