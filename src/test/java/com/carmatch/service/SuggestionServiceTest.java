package com.carmatch.service;

import com.carmatch.dto.response.CarResponse;
import com.carmatch.dto.response.SuggestionResponse;
import com.carmatch.entity.*;
import com.carmatch.enums.*;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.exception.SessionAlreadyActiveException;
import com.carmatch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private SwipeRepository swipeRepository;
    @Mock private SuggestionRepository suggestionRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserResponseRepository userResponseRepository;
    @Mock private CarService carService;
    @Mock private RecommendationService recommendationService;

    @InjectMocks
    private SuggestionService suggestionService;

    private User user;
    private Session session;
    private Car goodMatchCar;
    private Car weakMatchCar;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("zoa@gmail.com");

        session = new Session();
        session.setStatus(SessionStatus.COMPLETED);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "zoa@gmail.com", null, List.of()));

        when(userRepository.findByEmail("zoa@gmail.com"))
                .thenReturn(Optional.of(user));

        // A car that matches every criterion
        goodMatchCar = new Car();
        goodMatchCar.setBrand("Maruti");
        goodMatchCar.setModel("Swift");
        goodMatchCar.setPrice(700000.0);
        goodMatchCar.setFuelType(FuelType.PETROL);
        goodMatchCar.setCarType(CarType.HATCHBACK);
        goodMatchCar.setSeatingCapacity(5);
        goodMatchCar.setUsageType(UsageType.CITY);
        goodMatchCar.setTransmission(TransmissionType.MANUAL);
        goodMatchCar.setYear(2023);

        // A car that matches almost nothing
        weakMatchCar = new Car();
        weakMatchCar.setBrand("BMW");
        weakMatchCar.setModel("3 Series");
        weakMatchCar.setPrice(4500000.0);
        weakMatchCar.setFuelType(FuelType.DIESEL);
        weakMatchCar.setCarType(CarType.SEDAN);
        weakMatchCar.setSeatingCapacity(5);
        weakMatchCar.setUsageType(UsageType.HIGHWAY);
        weakMatchCar.setTransmission(TransmissionType.AUTOMATIC);
        weakMatchCar.setYear(2018);

    }

    private Swipe likeSwipe(Car car) {
        Swipe swipe = new Swipe();
        swipe.setCar(car);
        swipe.setDirection(SwipeDirection.LIKE);
        return swipe;
    }

    @Test
    void getSuggestions_ranksLikedCarsByScore_bestCarIsRankOne() {

        when(carService.mapToCarResponse(any())).thenReturn(new CarResponse());
        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(suggestionRepository.findBySessionIdOrderByRankPositionAsc(1L))
                .thenReturn(new ArrayList<>()); // nothing computed yet

        when(swipeRepository.findBySessionIdAndDirection(1L, SwipeDirection.LIKE))
                .thenReturn(List.of(likeSwipe(weakMatchCar), likeSwipe(goodMatchCar)));

        UserResponse ur = new UserResponse();
        ur.setQuestionKey("fuel_type");
        ur.setAnswerValue("PETROL");
        when(userResponseRepository.findBySessionId(1L)).thenReturn(List.of(ur));

        List<SuggestionResponse> result = suggestionService.getSuggestions(1L);

        // Best match car (Swift) should be ranked #1 despite being added second
        assertEquals(1, result.get(0).getRank());
        assertTrue(result.get(0).getScore() > result.get(1).getScore());
        verify(suggestionRepository, atLeastOnce()).save(any(Suggestion.class));
    }

    @Test
    void getSuggestions_noLikedCars_throwsException() {

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(suggestionRepository.findBySessionIdOrderByRankPositionAsc(1L))
                .thenReturn(new ArrayList<>());
        when(swipeRepository.findBySessionIdAndDirection(1L, SwipeDirection.LIKE))
                .thenReturn(new ArrayList<>()); // zero likes

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> suggestionService.getSuggestions(1L));

        assertTrue(ex.getMessage().contains("did not like any cars"));
    }

    @Test
    void getSuggestions_sessionStillActive_throwsException() {

        session.setStatus(SessionStatus.ACTIVE); // not completed yet

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));

        assertThrows(SessionAlreadyActiveException.class,
                () -> suggestionService.getSuggestions(1L));

        // Should never touch swipes if session isn't even completed
        verify(swipeRepository, never())
                .findBySessionIdAndDirection(anyLong(), any());
    }

    @Test
    void getSuggestions_alreadyComputed_returnsStoredResultWithoutRecalculating() {

        when(carService.mapToCarResponse(any())).thenReturn(new CarResponse());

        Suggestion stored = new Suggestion();
        stored.setCar(goodMatchCar);
        stored.setScore(115.0);
        stored.setRankPosition(1);
        stored.setReasoning("Already computed reasoning");

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(suggestionRepository.findBySessionIdOrderByRankPositionAsc(1L))
                .thenReturn(List.of(stored));

        List<SuggestionResponse> result = suggestionService.getSuggestions(1L);

        assertEquals(1, result.size());
        assertEquals("Already computed reasoning", result.get(0).getReasoning());
        // Should never re-check swipes since result was already stored
        verify(swipeRepository, never())
                .findBySessionIdAndDirection(anyLong(), any());
    }

    @Test
    void getSuggestions_limitsToTop3EvenWithMoreLikedCars() {

        when(carService.mapToCarResponse(any())).thenReturn(new CarResponse());
        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(suggestionRepository.findBySessionIdOrderByRankPositionAsc(1L))
                .thenReturn(new ArrayList<>());

        // 5 liked cars — should still only return top 3
        List<Swipe> fiveLikes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Car c = new Car();
            c.setBrand("Brand" + i);
            c.setFuelType(FuelType.PETROL);
            c.setCarType(CarType.HATCHBACK);
            c.setSeatingCapacity(5);
            c.setUsageType(UsageType.CITY);
            c.setTransmission(TransmissionType.MANUAL);
            c.setPrice(700000.0);
            c.setYear(2023);
            fiveLikes.add(likeSwipe(c));
        }
        when(swipeRepository.findBySessionIdAndDirection(1L, SwipeDirection.LIKE))
                .thenReturn(fiveLikes);

        UserResponse ur = new UserResponse();
        ur.setQuestionKey("fuel_type");
        ur.setAnswerValue("PETROL");
        when(userResponseRepository.findBySessionId(1L)).thenReturn(List.of(ur));

        List<SuggestionResponse> result = suggestionService.getSuggestions(1L);

        assertEquals(3, result.size());
    }
}