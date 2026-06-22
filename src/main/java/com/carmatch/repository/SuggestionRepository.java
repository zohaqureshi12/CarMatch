package com.carmatch.repository;

import com.carmatch.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    List<Suggestion> findBySessionIdOrderByRankPositionAsc(Long sessionId);
}