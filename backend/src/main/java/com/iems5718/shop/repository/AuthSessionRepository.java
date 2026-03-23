package com.iems5718.shop.repository;

import com.iems5718.shop.model.AuthSession;
import com.iems5718.shop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findByTokenHash(String tokenHash);
    void deleteByUser(User user);
    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
