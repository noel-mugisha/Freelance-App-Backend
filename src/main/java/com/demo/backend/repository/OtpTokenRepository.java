package com.demo.backend.repository;

import com.demo.backend.model.OtpToken;
import com.demo.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByUser(User user);
    Optional<OtpToken> findByToken(String token);
}