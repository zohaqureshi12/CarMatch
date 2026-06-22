package com.carmatch.repository;

import com.carmatch.entity.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {
    List<UserResponse> findBySessionId(Long sessionId);
}