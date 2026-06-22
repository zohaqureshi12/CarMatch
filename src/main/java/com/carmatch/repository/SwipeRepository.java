package com.carmatch.repository;

import com.carmatch.entity.Swipe;
import com.carmatch.enums.SwipeDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {
    List<Swipe> findBySessionId(Long sessionId);
    List<Swipe> findBySessionIdAndDirection(Long sessionId, SwipeDirection direction);
    boolean existsBySessionIdAndCarId(Long sessionId, Long carId);
}