package com.carmatch.service;

import com.carmatch.dto.request.SwipeRequest;
import com.carmatch.dto.response.SwipeResponse;
import com.carmatch.entity.Car;
import com.carmatch.entity.Session;
import com.carmatch.entity.Swipe;
import com.carmatch.entity.User;
import com.carmatch.enums.SessionStatus;
import com.carmatch.exception.DuplicateSwipeException;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.exception.SessionAlreadyActiveException;
import com.carmatch.repository.CarRepository;
import com.carmatch.repository.SessionRepository;
import com.carmatch.repository.SwipeRepository;
import com.carmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SwipeService {

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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

    // Record a Swipe
    public SwipeResponse recordSwipe(Long sessionId, SwipeRequest request) {

        // 1. Get current user
        User user = getCurrentUser();

        // 2. Find session and verify ownership
        Session session = sessionRepository
                .findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        // 3. Check session is still active
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new SessionAlreadyActiveException(
                    "This session is already completed. Start a new session.");
        }

        // 4. Find the car being swiped
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Car not found with id: " + request.getCarId()));

        // 5. Check for duplicate swipe
        if (swipeRepository.existsBySessionIdAndCarId(
                sessionId, request.getCarId())) {
            throw new DuplicateSwipeException(
                    "You already swiped on this car in this session");
        }

        // 6. Save the swipe
        Swipe swipe = new Swipe();
        swipe.setSession(session);
        swipe.setCar(car);
        swipe.setDirection(request.getDirection());
        swipe.setSwipedAt(LocalDateTime.now());
        swipeRepository.save(swipe);

        // 7. Check if all 20 cars have been swiped
        // If yes → auto complete the session
        long totalSwipes = swipeRepository
                .findBySessionId(sessionId).size();
        if (totalSwipes >= 20) {
            session.setStatus(SessionStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
            sessionRepository.save(session);
        }

        return mapToSwipeResponse(swipe);
    }

    // Get All Swipes for a Session
    public List<SwipeResponse> getSwipes(Long sessionId) {

        User user = getCurrentUser();

        // Verify session ownership
        sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with id: " + sessionId));

        return swipeRepository.findBySessionId(sessionId)
                .stream()
                .map(this::mapToSwipeResponse)
                .collect(Collectors.toList());
    }
}