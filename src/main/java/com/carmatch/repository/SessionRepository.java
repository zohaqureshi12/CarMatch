package com.carmatch.repository;

import com.carmatch.entity.Session;
import com.carmatch.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByUserId(Long userId);
    Optional<Session> findByUserIdAndStatus(Long userId, SessionStatus status);
}