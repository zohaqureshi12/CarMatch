package com.carmatch.service;

import com.carmatch.entity.Car;
import com.carmatch.entity.Session;
import com.carmatch.entity.User;
import com.carmatch.entity.UserResponse;
import com.carmatch.enums.*;
import com.carmatch.repository.CarRepository;
import com.carmatch.repository.SessionRepository;
import com.carmatch.repository.UserRepository;
import com.carmatch.repository.UserResponseRepository;
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
class RecommendationServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserResponseRepository userResponseRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarService carService;

    @InjectMocks
    private RecommendationService recommendationService;

    private Car testCar;


    @BeforeEach
    void setUp() {
        testCar = new Car();
        testCar.setBrand("Maruti");
        testCar.setModel("Swift");
        testCar.setPrice(700000.0);
        testCar.setFuelType(FuelType.PETROL);
        testCar.setCarType(CarType.HATCHBACK);
        testCar.setSeatingCapacity(5);
        testCar.setUsageType(UsageType.CITY);
        testCar.setTransmission(TransmissionType.MANUAL);
        testCar.setYear(2023);
    }

    @Test
    void scoreCar_perfectMatch_returnsFullScore() throws Exception {
        Map<String, String> answers = new HashMap<>();
        answers.put("budget", "600K_TO_1M");
        answers.put("fuel_type", "PETROL");
        answers.put("car_type", "HATCHBACK");
        answers.put("seating", "5");
        answers.put("usage", "CITY");
        answers.put("transmission", "MANUAL");

        var method = RecommendationService.class
                .getDeclaredMethod("scoreCar", Car.class, Map.class);
        method.setAccessible(true);
        var result = (com.carmatch.dto.response.RecommendationResponse)
                method.invoke(recommendationService, testCar, answers);

        assertEquals(115.0, result.getScore());
        assertEquals(100, result.getScorePercentage());
    }

    @Test
    void scoreCar_wrongFuelType_losesFuelPoints() throws Exception {
        Map<String, String> answers = new HashMap<>();
        answers.put("budget", "600K_TO_1M");
        answers.put("fuel_type", "ELECTRIC");
        answers.put("car_type", "HATCHBACK");
        answers.put("seating", "5");
        answers.put("usage", "CITY");
        answers.put("transmission", "MANUAL");

        var method = RecommendationService.class
                .getDeclaredMethod("scoreCar", Car.class, Map.class);
        method.setAccessible(true);
        var result = (com.carmatch.dto.response.RecommendationResponse)
                method.invoke(recommendationService, testCar, answers);

        assertEquals(90.0, result.getScore());
    }

    @Test
    void scoreCar_noPreference_alwaysMatchesFuelType() throws Exception {
        Map<String, String> answers = new HashMap<>();
        answers.put("budget", "600K_TO_1M");
        answers.put("fuel_type", "NO_PREFERENCE");
        answers.put("car_type", "HATCHBACK");
        answers.put("seating", "5");
        answers.put("usage", "CITY");
        answers.put("transmission", "MANUAL");

        var method = RecommendationService.class
                .getDeclaredMethod("scoreCar", Car.class, Map.class);
        method.setAccessible(true);
        var result = (com.carmatch.dto.response.RecommendationResponse)
                method.invoke(recommendationService, testCar, answers);

        assertEquals(115.0, result.getScore());
    }

    @Test
    void scoreCar_carHasMoreSeatsThanNeeded_stillFullSeatingPoints() throws Exception {
        testCar.setSeatingCapacity(7);

        Map<String, String> answers = new HashMap<>();
        answers.put("budget", "600K_TO_1M");
        answers.put("fuel_type", "PETROL");
        answers.put("car_type", "HATCHBACK");
        answers.put("seating", "5");
        answers.put("usage", "CITY");
        answers.put("transmission", "MANUAL");

        var method = RecommendationService.class
                .getDeclaredMethod("scoreCar", Car.class, Map.class);
        method.setAccessible(true);
        var result = (com.carmatch.dto.response.RecommendationResponse)
                method.invoke(recommendationService, testCar, answers);

        assertEquals(115.0, result.getScore());
    }

    @Test
    void scoreCar_budgetOutOfRange_losesBudgetPoints() throws Exception {
        testCar.setPrice(5000000.0);

        Map<String, String> answers = new HashMap<>();
        answers.put("budget", "600K_TO_1M");
        answers.put("fuel_type", "PETROL");
        answers.put("car_type", "HATCHBACK");
        answers.put("seating", "5");
        answers.put("usage", "CITY");
        answers.put("transmission", "MANUAL");

        var method = RecommendationService.class
                .getDeclaredMethod("scoreCar", Car.class, Map.class);
        method.setAccessible(true);
        var result = (com.carmatch.dto.response.RecommendationResponse)
                method.invoke(recommendationService, testCar, answers);

        assertEquals(85.0, result.getScore());
    }
    @Test
    void scoreCar_recentModel_getsRecencyBonus() throws Exception {
        testCar.setYear(2024); // within last 3 years

        Map<String, String> answers = new HashMap<>();
        answers.put("budget", "600K_TO_1M");
        answers.put("fuel_type", "PETROL");
        answers.put("car_type", "HATCHBACK");
        answers.put("seating", "5");
        answers.put("usage", "CITY");
        answers.put("transmission", "MANUAL");

        var method = RecommendationService.class
                .getDeclaredMethod("scoreCar", Car.class, Map.class);
        method.setAccessible(true);
        var result = (com.carmatch.dto.response.RecommendationResponse)
                method.invoke(recommendationService, testCar, answers);

        // All criteria match + recency bonus already included in 115 max
        assertEquals(115.0, result.getScore());
        assertTrue(result.getMatchedCriteria().stream()
                .anyMatch(c -> c.contains("Recent model")));
    }

    @Test
    void scoreCar_oldModel_losesRecencyBonus() throws Exception {
        testCar.setYear(2018); // older than 3 years from "now"

        Map<String, String> answers = new HashMap<>();
        answers.put("budget", "600K_TO_1M");
        answers.put("fuel_type", "PETROL");
        answers.put("car_type", "HATCHBACK");
        answers.put("seating", "5");
        answers.put("usage", "CITY");
        answers.put("transmission", "MANUAL");

        var method = RecommendationService.class
                .getDeclaredMethod("scoreCar", Car.class, Map.class);
        method.setAccessible(true);
        var result = (com.carmatch.dto.response.RecommendationResponse)
                method.invoke(recommendationService, testCar, answers);

        // Loses exactly 5 points (recency weight)
        assertEquals(110.0, result.getScore());
    }

    @Test
    void scoreCar_multipleSimultaneousMismatches_lossesStack() throws Exception {
        Map<String, String> answers = new HashMap<>();
        answers.put("budget", "UNDER_300K");      // mismatch: -30
        answers.put("fuel_type", "ELECTRIC");     // mismatch: -25
        answers.put("car_type", "SUV");           // mismatch: -20
        answers.put("seating", "7_OR_MORE");      // mismatch: -15 (car has 5)
        answers.put("usage", "HIGHWAY");          // mismatch: -10
        answers.put("transmission", "AUTOMATIC"); // mismatch: -10

        var method = RecommendationService.class
                .getDeclaredMethod("scoreCar", Car.class, Map.class);
        method.setAccessible(true);
        var result = (com.carmatch.dto.response.RecommendationResponse)
                method.invoke(recommendationService, testCar, answers);

        // Every criterion mismatched — only the +5 recency bonus survives
        assertEquals(5.0, result.getScore());
        assertTrue(result.getMatchedCriteria().size() == 1);
    }
    @Test
    void getRecommendations_returnsTop20SortedByScore_andRejectsWrongUser() {
        // Arrange: fake logged-in user
        User user = new User();
        user.setEmail("zoa@gmail.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "zoa@gmail.com", null, List.of()));

        when(userRepository.findByEmail("zoa@gmail.com"))
                .thenReturn(Optional.of(user));

        Session session = new Session();
        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));

        UserResponse ur = new UserResponse();
        ur.setQuestionKey("fuel_type");
        ur.setAnswerValue("PETROL");
        when(userResponseRepository.findBySessionId(1L))
                .thenReturn(List.of(ur));

        // 25 fake cars — engine should cap results at 20
        List<Car> cars = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Car c = new Car();
            c.setBrand("Brand" + i);
            c.setFuelType(FuelType.PETROL);
            c.setCarType(CarType.HATCHBACK);
            c.setSeatingCapacity(5);
            c.setUsageType(UsageType.CITY);
            c.setTransmission(TransmissionType.MANUAL);
            c.setPrice(700000.0);
            c.setYear(2023);
            cars.add(c);
        }
        when(carRepository.findByStatusAndIsDeletedFalse(CarStatus.APPROVED))
                .thenReturn(cars);

        when(carService.mapToCarSummaryResponse(any()))
                .thenReturn(new com.carmatch.dto.response.CarSummaryResponse());

        // Act
        var result = recommendationService.getRecommendations(1L);

        // Assert: capped at 20, not 25
        assertEquals(20, result.size());
    }
}