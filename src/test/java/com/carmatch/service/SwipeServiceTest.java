package com.carmatch.service;

import com.carmatch.dto.request.SwipeRequest;
import com.carmatch.dto.response.SwipeResponse;
import com.carmatch.entity.Car;
import com.carmatch.entity.Session;
import com.carmatch.entity.Swipe;
import com.carmatch.entity.User;
import com.carmatch.enums.SessionStatus;
import com.carmatch.enums.SwipeDirection;
import com.carmatch.exception.DuplicateSwipeException;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.exception.SessionAlreadyActiveException;
import com.carmatch.repository.CarRepository;
import com.carmatch.repository.SessionRepository;
import com.carmatch.repository.SwipeRepository;
import com.carmatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwipeServiceTest {

    @Mock
    private SwipeRepository swipeRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SwipeService swipeService;

    private User user;
    private Session session;
    private Car car;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("zoa@gmail.com");

        session = new Session();
        session.setUser(user);
        session.setStatus(SessionStatus.ACTIVE);

        car = new Car();
        car.setBrand("Maruti");
        car.setModel("Swift");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "zoa@gmail.com", null, List.of()));

        when(userRepository.findByEmail("zoa@gmail.com"))
                .thenReturn(Optional.of(user));
    }

    @Test
    void recordSwipe_newCar_savesSuccessfully() {
        SwipeRequest request = new SwipeRequest();
        request.setCarId(1L);
        request.setDirection(SwipeDirection.LIKE);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(swipeRepository.existsBySessionIdAndCarId(1L, 1L))
                .thenReturn(false);
        when(swipeRepository.findBySessionId(1L))
                .thenReturn(List.of(new Swipe())); // only 1 swipe so far

        SwipeResponse response = swipeService.recordSwipe(1L, request);

        assertEquals(SwipeDirection.LIKE, response.getDirection());
        verify(swipeRepository, times(1)).save(any(Swipe.class));
        // Session should NOT be marked completed with only 1 swipe
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void recordSwipe_duplicateCar_throwsException() {
        SwipeRequest request = new SwipeRequest();
        request.setCarId(1L);
        request.setDirection(SwipeDirection.LIKE);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(swipeRepository.existsBySessionIdAndCarId(1L, 1L))
                .thenReturn(true); // already swiped

        assertThrows(DuplicateSwipeException.class,
                () -> swipeService.recordSwipe(1L, request));

        verify(swipeRepository, never()).save(any(Swipe.class));
    }

    @Test
    void recordSwipe_completedSession_throwsException() {
        session.setStatus(SessionStatus.COMPLETED);

        SwipeRequest request = new SwipeRequest();
        request.setCarId(1L);
        request.setDirection(SwipeDirection.LIKE);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));

        assertThrows(SessionAlreadyActiveException.class,
                () -> swipeService.recordSwipe(1L, request));
    }

    @Test
    void recordSwipe_20thSwipe_autoCompletesSession() {
        SwipeRequest request = new SwipeRequest();
        request.setCarId(20L);
        request.setDirection(SwipeDirection.PASS);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(carRepository.findById(20L)).thenReturn(Optional.of(car));
        when(swipeRepository.existsBySessionIdAndCarId(1L, 20L))
                .thenReturn(false);

        // Simulate 20 total swipes now recorded (including this one)
        List<Swipe> twentySwipes = new ArrayList<>();
        for (int i = 0; i < 20; i++) twentySwipes.add(new Swipe());
        when(swipeRepository.findBySessionId(1L)).thenReturn(twentySwipes);

        swipeService.recordSwipe(1L, request);

        // Session must be saved as COMPLETED
        verify(sessionRepository, times(1)).save(session);
        assertEquals(SessionStatus.COMPLETED, session.getStatus());
        assertNotNull(session.getCompletedAt());
    }

    @Test
    void recordSwipe_carNotFound_throwsException() {
        SwipeRequest request = new SwipeRequest();
        request.setCarId(999L);
        request.setDirection(SwipeDirection.LIKE);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(carRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> swipeService.recordSwipe(1L, request));
    }
}