package com.project.auth.app.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.auth.app.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

		Optional<User> findByEmail(String email);

		boolean existsByEmail(String email);

}
