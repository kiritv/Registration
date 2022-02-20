package com.ishtec.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ishtec.server.entities.LookupRole;

public interface LookupRoleRepository extends JpaRepository<LookupRole, Long> {
    Optional<LookupRole> findByRoleName(String string);
}
