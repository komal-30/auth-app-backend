package com.project.auth.app.backend.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User  implements UserDetails{

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "user_id")
	private UUID id;

	@Column(name = "user_email", unique = true, length = 300)
	private String email;

	@Column(name = "user_name", length = 500)
	private String name;

	private String password;
	private String image;
	private boolean enable = true;

	@CreatedDate
	private Instant createdAt;

	@LastModifiedDate
	private Instant updatedAt;

	@PrePersist
	public void onCreate() {
		this.createdAt = Instant.now();
	}

	@PreUpdate
	public void onUpdat() {
		this.updatedAt = Instant.now();
	}

	@Enumerated(EnumType.STRING)
	private Provider provider = Provider.LOCAL;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.email;
	}



}
