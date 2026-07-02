package com.carmatch.service;

import com.carmatch.dto.response.CarResponse;
import com.carmatch.dto.response.SuggestionResponse;
import com.carmatch.entity.Car;
import com.carmatch.entity.Session;
import com.carmatch.entity.Suggestion;
import com.carmatch.entity.User;
import com.carmatch.entity.UserResponse;
import com.carmatch.enums.SessionStatus;
import com.carmatch.enums.SwipeDirection;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.exception.SessionAlreadyActiveException;
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
public class SuggestionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserResponseRepository userResponseRepository;

    @Autowired
    private CarService carService;

    @Autowired
    private RecommendationService recommendationService;

    private static final double MAX_SCORE = 115.0;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SuggestionResponse mapToSuggestionResponse(Suggestion suggestion) {
        SuggestionResponse response = new SuggestionResponse();
        response.setRank(suggestion.getRankPosition());
        response.setScore(suggestion.getScore());
        response.setScorePercentage(
                (int) Math.round((suggestion.getScore() / MAX_SCORE) * 100));
        response.setReasoning(suggestion.getReasoning());

        // Map car entity to full CarResponse
        CarResponse carResponse = carService.mapToCarResponse(suggestion.getCar());
        response.setCar(carResponse);

        return response;
    }

    public List<SuggestionResponse> getSuggestions(Long sessionId) {

        // 1. Get current user
        User user = getCurrentUser();

        // 2. Find session and verify ownership
        Session session = sessionRepository
                .findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        // 3. Session must be completed
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new SessionAlreadyActiveException(
                    "Session is still active. Complete all swipes first.");
        }

        // 4. If suggestions already computed → return stored ones
        List<Suggestion> existing =
                suggestionRepository.findBySessionIdOrderByRankPositionAsc(sessionId);
        if (!existing.isEmpty()) {
            return existing.stream()
                    .map(this::mapToSuggestionResponse)
                    .collect(Collectors.toList());
        }

        // 5. Get all LIKED cars from this session
        List<Car> likedCars = swipeRepository
                .findBySessionIdAndDirection(sessionId, SwipeDirection.LIKE)
                .stream()
                .map(swipe -> swipe.getCar())
                .collect(Collectors.toList());

        if (likedCars.isEmpty()) {
            throw new ResourceNotFoundException(
                    "You did not like any cars in this session. " +
                            "Start a new session and swipe right on cars you like.");
        }

        // 6. Load user's answers for scoring
        List<UserResponse> responses =
                userResponseRepository.findBySessionId(sessionId);

        Map<String, String> answerMap = responses.stream()
                .collect(Collectors.toMap(
                        UserResponse::getQuestionKey,
                        UserResponse::getAnswerValue));

        // 7. Re-score liked cars using same algorithm
        // Sort by score descending, take top 2
        List<Car> topCars = likedCars.stream()
                .sorted(Comparator.comparingDouble(car ->
                        -calculateScore(car, answerMap)))
                .limit(3)
                .collect(Collectors.toList());

        // 8. Generate suggestion with reasoning for each top car
        List<SuggestionResponse> result = new ArrayList<>();

        for (int i = 0; i < topCars.size(); i++) {
            Car car = topCars.get(i);
            double score = calculateScore(car, answerMap);
            int rank = i + 1;

            String reasoning = generateReasoning(car, answerMap, score, rank);

            // Save to DB
            Suggestion suggestion = new Suggestion();
            suggestion.setSession(session);
            suggestion.setCar(car);
            suggestion.setScore(score);
            suggestion.setRankPosition(rank);
            suggestion.setReasoning(reasoning);
            suggestionRepository.save(suggestion);

            SuggestionResponse response = new SuggestionResponse();
            response.setRank(rank);
            response.setScore(score);
            response.setScorePercentage(
                    (int) Math.round((score / MAX_SCORE) * 100));
            response.setReasoning(reasoning);
            response.setCar(carService.mapToCarResponse(car));

            result.add(response);
        }

        return result;
    }

    // ── Score a single car against user answers ───────────────────
    private double calculateScore(Car car, Map<String, String> answers) {
        double score = 0;

        // Budget (30 pts)
        String budget = answers.getOrDefault("budget", "");
        double price = car.getPrice();
        boolean budgetMatch = switch (budget) {
            case "UNDER_300K"   -> price < 300000;
            case "300K_TO_600K" -> price >= 300000 && price < 600000;
            case "600K_TO_1M"   -> price >= 600000 && price < 1000000;
            case "ABOVE_1M"     -> price >= 1000000;
            default -> false;
        };
        if (budgetMatch) score += 30;

        // Fuel type (25 pts)
        String fuel = answers.getOrDefault("fuel_type", "NO_PREFERENCE");
        if (fuel.equals("NO_PREFERENCE") || fuel.equals(car.getFuelType().name()))
            score += 25;

        // Car type (20 pts)
        String carType = answers.getOrDefault("car_type", "NO_PREFERENCE");
        if (carType.equals("NO_PREFERENCE") || carType.equals(car.getCarType().name()))
            score += 20;

        // Seating (15 pts)
        String seating = answers.getOrDefault("seating", "");
        int required = switch (seating) {
            case "2" -> 2;
            case "4" -> 4;
            case "5" -> 5;
            case "7_OR_MORE" -> 7;
            default -> 0;
        };
        if (car.getSeatingCapacity() >= required) score += 15;

        // Usage (10 pts)
        String usage = answers.getOrDefault("usage", "");
        if (usage.equals(car.getUsageType().name())) score += 10;

        // Transmission (10 pts)
        String trans = answers.getOrDefault("transmission", "NO_PREFERENCE");
        if (trans.equals("NO_PREFERENCE") || trans.equals(car.getTransmission().name()))
            score += 10;

        // Recency bonus (5 pts)
        if (car.getYear() >= java.time.LocalDate.now().getYear() - 3)
            score += 5;

        return score;
    }

    // ── Generate human readable reasoning ────────────────────────
    private String generateReasoning(
            Car car, Map<String, String> answers, double score, int rank) {

        String rankText = switch (rank) {
            case 1 -> "best";
            case 2 -> "second best";
            case 3 -> "third best";
            default -> "top";
        };
        int percentage = (int) Math.round((score / MAX_SCORE) * 100);

        List<String> reasons = new ArrayList<>();

        String budget = answers.getOrDefault("budget", "");
        double price = car.getPrice();
        boolean budgetMatch = switch (budget) {
            case "UNDER_300K"   -> price < 300000;
            case "300K_TO_600K" -> price >= 300000 && price < 600000;
            case "600K_TO_1M"   -> price >= 600000 && price < 1000000;
            case "ABOVE_1M"     -> price >= 1000000;
            default -> false;
        };
        if (budgetMatch) reasons.add("fits your budget");

        String fuel = answers.getOrDefault("fuel_type", "NO_PREFERENCE");
        if (!fuel.equals("NO_PREFERENCE") && fuel.equals(car.getFuelType().name()))
            reasons.add("matches your " + fuel.toLowerCase() + " fuel preference");

        String carType = answers.getOrDefault("car_type", "NO_PREFERENCE");
        if (!carType.equals("NO_PREFERENCE") && carType.equals(car.getCarType().name()))
            reasons.add("is the " + carType.toLowerCase() + " type you wanted");

        String usage = answers.getOrDefault("usage", "");
        if (usage.equals(car.getUsageType().name()))
            reasons.add("is suited for " + usage.toLowerCase() + " driving");

        String trans = answers.getOrDefault("transmission", "NO_PREFERENCE");
        if (!trans.equals("NO_PREFERENCE") && trans.equals(car.getTransmission().name()))
            reasons.add("has your preferred " + trans.toLowerCase() + " transmission");

        String reasonText = reasons.isEmpty()
                ? "it scored well across multiple criteria"
                : String.join(", ", reasons);

        return String.format(
                "The %s %s is your %s match because it %s, scoring %d%% overall.",
                car.getBrand(), car.getModel(), rankText, reasonText, percentage);
    }
}