package com.carmatch.service;

import com.carmatch.dto.request.SubmitResponsesRequest;
import com.carmatch.dto.response.SessionResponse;
import com.carmatch.entity.Session;
import com.carmatch.entity.User;
import com.carmatch.entity.UserResponse;
import com.carmatch.enums.SessionStatus;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.exception.SessionAlreadyActiveException;
import com.carmatch.repository.SessionRepository;
import com.carmatch.repository.UserRepository;
import com.carmatch.repository.UserResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserResponseRepository userResponseRepository;

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
        response.setResponseCount(
                session.getResponses() != null ? session.getResponses().size() : 0);
        response.setSwipeCount(
                session.getSwipes() != null ? session.getSwipes().size() : 0);
        return response;
    }

    //  Get 6 Questions
    public List<Map<String, Object>> getQuestions() {
        List<Map<String, Object>> questions = new ArrayList<>();

        questions.add(createQuestion("budget", "What is your total budget?",
                List.of("UNDER_300K", "300K_TO_600K", "600K_TO_1M", "ABOVE_1M")));

        questions.add(createQuestion("fuel_type", "Which fuel type do you prefer?",
                List.of("PETROL", "DIESEL", "ELECTRIC", "HYBRID", "NO_PREFERENCE")));

        questions.add(createQuestion("car_type", "What type of car are you looking for?",
                List.of("SEDAN", "SUV", "HATCHBACK", "COUPE", "TRUCK", "VAN", "NO_PREFERENCE")));

        questions.add(createQuestion("seating", "How many seats do you need?",
                List.of("2", "4", "5", "7_OR_MORE")));

        questions.add(createQuestion("usage", "How will you mainly use this car?",
                List.of("CITY", "HIGHWAY", "MIXED")));

        questions.add(createQuestion("transmission", "Transmission preference?",
                List.of("MANUAL", "AUTOMATIC", "NO_PREFERENCE")));

        return questions;
    }

    private Map<String, Object> createQuestion(
            String key, String text, List<String> options) {
        Map<String, Object> question = new LinkedHashMap<>();
        question.put("questionKey", key);
        question.put("questionText", text);
        question.put("options", options);
        return question;
    }

    //  Start Session
    public SessionResponse startSession() {
        User user = getCurrentUser();

        sessionRepository.findByUserIdAndStatus(user.getId(), SessionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new SessionAlreadyActiveException(
                            "You already have an active session. Complete it before starting a new one.");
                });

        Session session = new Session();
        session.setUser(user);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return mapToSessionResponse(session);
    }

    // Submit Responses
    public SessionResponse submitResponses(
            Long sessionId, SubmitResponsesRequest request) {

        User user = getCurrentUser();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Session not found");
        }

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new SessionAlreadyActiveException(
                    "This session is already completed");
        }

        // Delete existing responses if resubmitting
        userResponseRepository.deleteAll(
                userResponseRepository.findBySessionId(sessionId));

        // Save new responses
        for (SubmitResponsesRequest.QuestionAnswer qa : request.getResponses()) {
            UserResponse response = new UserResponse();
            response.setSession(session);
            response.setQuestionKey(qa.getQuestionKey());
            response.setAnswerValue(qa.getAnswerValue());
            userResponseRepository.save(response);
        }

        return mapToSessionResponse(session);
    }

    //  Get Session
    public SessionResponse getSession(Long sessionId) {
        User user = getCurrentUser();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Session not found");
        }

        return mapToSessionResponse(session);
    }
}