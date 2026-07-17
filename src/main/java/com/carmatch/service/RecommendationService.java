package com.carmatch.service;

import com.carmatch.dto.response.CarSummaryResponse;
import com.carmatch.dto.response.RecommendationResponse;
import com.carmatch.entity.Car;
import com.carmatch.entity.UserResponse;
import com.carmatch.enums.CarStatus;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.repository.CarRepository;
import com.carmatch.repository.SessionRepository;
import com.carmatch.repository.UserRepository;
import com.carmatch.repository.UserResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserResponseRepository userResponseRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarService carService;


    private static final double MAX_SCORE = 115.0;

    public List<RecommendationResponse> getRecommendations(Long sessionId) {


        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        com.carmatch.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));


        List<UserResponse> responses =
                userResponseRepository.findBySessionId(sessionId);

        if (responses.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No responses found for session: " + sessionId
                            + ". Please submit your questionnaire answers first.");
        }


        Map<String, String> answerMap = responses.stream()
                .collect(Collectors.toMap(
                        UserResponse::getQuestionKey,
                        UserResponse::getAnswerValue));


        List<Car> allCars = carRepository
                .findByStatusAndIsDeletedFalse(CarStatus.APPROVED);

        if (allCars.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No approved cars available for recommendation.");
        }


        return allCars.stream()
                .map(car -> scoreCar(car, answerMap))
                .sorted(Comparator.comparingDouble(
                        RecommendationResponse::getScore).reversed())
                .limit(20)
                .collect(Collectors.toList());
    }

    // Scoring Algorithm
    private RecommendationResponse scoreCar(
            Car car, Map<String, String> answers) {

        double score = 0;
        List<String> matchedCriteria = new ArrayList<>();

        // 1. Budget (30 points)
        String budget = answers.getOrDefault("budget", "");
        double price = car.getPrice();

        boolean budgetMatch = false;
        switch (budget) {
            case "UNDER_300K"    -> budgetMatch = price < 300000;
            case "300K_TO_600K"  -> budgetMatch = price >= 300000 && price < 600000;
            case "600K_TO_1M"    -> budgetMatch = price >= 600000 && price < 1000000;
            case "ABOVE_1M"      -> budgetMatch = price >= 1000000;
        }
        if (budgetMatch) {
            score += 30;
            matchedCriteria.add("Budget match");
        }

        // 2. Fuel Type (25 points)
        String fuelPref = answers.getOrDefault("fuel_type", "NO_PREFERENCE");
        if (fuelPref.equals("NO_PREFERENCE") ||
                fuelPref.equals(car.getFuelType().name())) {
            score += 25;
            matchedCriteria.add("Fuel type: " + car.getFuelType().name());
        }

        // 3. Car Type (20 points)
        String carTypePref = answers.getOrDefault("car_type", "NO_PREFERENCE");
        if (carTypePref.equals("NO_PREFERENCE") ||
                carTypePref.equals(car.getCarType().name())) {
            score += 20;
            matchedCriteria.add("Car type: " + car.getCarType().name());
        }

        // 4. Seating Capacity (15 points)
        String seatingPref = answers.getOrDefault("seating", "");
        int requiredSeats = 0;
        switch (seatingPref) {
            case "2"         -> requiredSeats = 2;
            case "4"         -> requiredSeats = 4;
            case "5"         -> requiredSeats = 5;
            case "7_OR_MORE" -> requiredSeats = 7;
        }
        // Car seats >= required → full points (more seats is fine)
        if (car.getSeatingCapacity() >= requiredSeats) {
            score += 15;
            matchedCriteria.add("Seating: " + car.getSeatingCapacity() + " seats");
        }

        //5. Usage Type (10 points)
        String usagePref = answers.getOrDefault("usage", "");
        if (usagePref.equals(car.getUsageType().name())) {
            score += 10;
            matchedCriteria.add("Usage: " + car.getUsageType().name());
        }

        // 6. Transmission (10 points)
        String transPref = answers.getOrDefault("transmission", "NO_PREFERENCE");
        if (transPref.equals("NO_PREFERENCE") ||
                transPref.equals(car.getTransmission().name())) {
            score += 10;
            matchedCriteria.add("Transmission: " + car.getTransmission().name());
        }

        // 7. Recency Bonus (5 points)
        int currentYear = LocalDate.now().getYear();
        if (car.getYear() >= currentYear - 3) {
            score += 5;
            matchedCriteria.add("Recent model: " + car.getYear());
        }

        //Build Response
        RecommendationResponse response = new RecommendationResponse();
        response.setCar(carService.mapToCarSummaryResponse(car));
        response.setScore(score);
        response.setScorePercentage((int) Math.round((score / MAX_SCORE) * 100));
        response.setMatchedCriteria(matchedCriteria);

        return response;
    }
}