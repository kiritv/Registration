package com.ishtec.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ishtec.server.entities.LookupStatus;

import java.util.Optional;

public interface LookupStatusRepository extends JpaRepository<LookupStatus, Long> {
    Optional<LookupStatus> findByStatusName(String string);
}
