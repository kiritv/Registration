package com.ishtec.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ishtec.server.entities.UserSecret;

import java.util.Optional;

public interface UserSecretRepository extends JpaRepository<UserSecret, Long> {
    Optional<UserSecret> findByEmailIgnoreCase(String email);
}
