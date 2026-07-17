package com.carmatch.service;

import com.carmatch.dto.response.SessionResponse;
import com.carmatch.dto.response.SuggestionResponse;
import com.carmatch.dto.response.SwipeResponse;
import com.carmatch.entity.Session;
import com.carmatch.entity.Suggestion;
import com.carmatch.entity.Swipe;
import com.carmatch.entity.User;
import com.carmatch.entity.UserResponse;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.repository.SessionRepository;
import com.carmatch.repository.SuggestionRepository;
import com.carmatch.repository.SwipeRepository;
import com.carmatch.repository.UserRepository;
import com.carmatch.repository.UserResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private UserResponseRepository userResponseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarService carService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SessionResponse mapToSessionResponse(Session session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setStatus(session.getStatus());
        response.setStartedAt(session.getStartedAt());
        response.setCompletedAt(session.getCompletedAt());

        int responseCount = userResponseRepository
                .findBySessionId(session.getId()).size();
        response.setResponseCount(responseCount);

        int swipeCount = swipeRepository
                .findBySessionId(session.getId()).size();
        response.setSwipeCount(swipeCount);

        return response;
    }

    private SwipeResponse mapToSwipeResponse(Swipe swipe) {
        SwipeResponse response = new SwipeResponse();
        response.setId(swipe.getId());
        response.setCarId(swipe.getCar().getId());
        response.setCarBrand(swipe.getCar().getBrand());
        response.setCarModel(swipe.getCar().getModel());
        response.setDirection(swipe.getDirection());
        response.setSwipedAt(swipe.getSwipedAt());
        return response;
    }

    private SuggestionResponse mapToSuggestionResponse(Suggestion suggestion) {
        SuggestionResponse response = new SuggestionResponse();
        response.setRank(suggestion.getRankPosition());
        response.setScore(suggestion.getScore());
        response.setScorePercentage(
                (int) Math.round((suggestion.getScore() / 115.0) * 100));
        response.setReasoning(suggestion.getReasoning());
        response.setCar(carService.mapToCarResponse(suggestion.getCar()));
        return response;
    }

    // All Sessions for Current User
    public List<SessionResponse> getAllSessions() {
        User user = getCurrentUser();

        return sessionRepository.findByUserId(user.getId())
                .stream()
                .sorted(Comparator.comparing(Session::getStartedAt).reversed())
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    // Full Detail of One Session
    public Map<String, Object> getSessionDetail(Long sessionId) {
        User user = getCurrentUser();

        Session session = sessionRepository
                .findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        List<UserResponse> answers =
                userResponseRepository.findBySessionId(sessionId);

        List<SwipeResponse> swipes = swipeRepository
                .findBySessionId(sessionId)
                .stream()
                .map(this::mapToSwipeResponse)
                .collect(Collectors.toList());

        List<SuggestionResponse> suggestions = suggestionRepository
                .findBySessionIdOrderByRankPositionAsc(sessionId)
                .stream()
                .map(this::mapToSuggestionResponse)
                .collect(Collectors.toList());

        Map<String, String> answerMap = answers.stream()
                .collect(Collectors.toMap(
                        UserResponse::getQuestionKey,
                        UserResponse::getAnswerValue));

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("session", mapToSessionResponse(session));
        detail.put("answers", answerMap);
        detail.put("swipes", swipes);
        detail.put("suggestions", suggestions);

        return detail;
    }

    // All Suggestions Across All Sessions
    public List<SuggestionResponse> getAllSuggestions() {
        User user = getCurrentUser();

        List<Session> userSessions = sessionRepository.findByUserId(user.getId());

        List<SuggestionResponse> allSuggestions = new ArrayList<>();

        for (Session session : userSessions) {
            List<Suggestion> suggestions = suggestionRepository
                    .findBySessionIdOrderByRankPositionAsc(session.getId());
            for (Suggestion s : suggestions) {
                allSuggestions.add(mapToSuggestionResponse(s));
            }
        }

        return allSuggestions;
    }
}