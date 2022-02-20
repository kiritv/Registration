package com.ishtec.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ishtec.server.entities.UserToken;

public interface UserTokenRepository extends JpaRepository<UserToken, Long>  {
    @Query("SELECT t FROM UserToken t WHERE t.token = :token")
    Optional<UserToken> findByToken(@Param("token") String token);
    //@Query("SELECT t FROM UserToken t WHERE t.email = :email")
    List<UserToken> findByEmail(@Param("email") String email);
    long deleteByEmail(String email);
}
