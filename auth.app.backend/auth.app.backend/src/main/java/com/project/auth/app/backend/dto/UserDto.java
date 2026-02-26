package com.project.auth.app.backend.dto;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.project.auth.app.backend.entity.Provider;
import com.project.auth.app.backend.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDto {

	private UUID id;
	private String email;
	private String name;
	private String password;
	private String image;
	private boolean enable = true;
	private Instant createdAt;
	private Instant updatedAt;
	private Provider provider = Provider.LOCAL;
	private Set<Role> roles = new HashSet<>();

}