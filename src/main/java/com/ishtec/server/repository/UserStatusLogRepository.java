package com.ishtec.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ishtec.server.entities.UserStatusLog;

public interface UserStatusLogRepository extends JpaRepository<UserStatusLog, Long> {
}
