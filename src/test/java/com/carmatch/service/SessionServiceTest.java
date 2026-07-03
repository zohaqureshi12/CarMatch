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
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserResponseRepository userResponseRepository;

    @InjectMocks
    private SessionService sessionService;

    private User user;
    private Session session;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("zoa@gmail.com");

        session = new Session();
        session.setStatus(SessionStatus.ACTIVE);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "zoa@gmail.com", null, List.of()));

        when(userRepository.findByEmail("zoa@gmail.com"))
                .thenReturn(Optional.of(user));
    }

    private SubmitResponsesRequest.QuestionAnswer qa(String key, String value) {
        SubmitResponsesRequest.QuestionAnswer answer =
                new SubmitResponsesRequest.QuestionAnswer();
        answer.setQuestionKey(key);
        answer.setAnswerValue(value);
        return answer;
    }

    @Test
    void submitResponses_allSixKeysPresent_savesSuccessfully() {
        List<SubmitResponsesRequest.QuestionAnswer> answers = new ArrayList<>();
        answers.add(qa("budget", "600K_TO_1M"));
        answers.add(qa("fuel_type", "PETROL"));
        answers.add(qa("car_type", "HATCHBACK"));
        answers.add(qa("seating", "5"));
        answers.add(qa("usage", "CITY"));
        answers.add(qa("transmission", "MANUAL"));

        SubmitResponsesRequest request = new SubmitResponsesRequest();
        request.setResponses(answers);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));
        when(userResponseRepository.findBySessionId(1L))
                .thenReturn(new ArrayList<>());
        when(sessionRepository.findById(1L))
                .thenReturn(Optional.of(session));

        SessionResponse response = sessionService.submitResponses(1L, request);

        assertNotNull(response);
        // Should save exactly 6 responses
        verify(userResponseRepository, times(6)).save(any(UserResponse.class));
    }

    @Test
    void submitResponses_missingOneKey_throwsException() {
        // Only 5 keys — missing "transmission"
        List<SubmitResponsesRequest.QuestionAnswer> answers = new ArrayList<>();
        answers.add(qa("budget", "600K_TO_1M"));
        answers.add(qa("fuel_type", "PETROL"));
        answers.add(qa("car_type", "HATCHBACK"));
        answers.add(qa("seating", "5"));
        answers.add(qa("usage", "CITY"));

        SubmitResponsesRequest request = new SubmitResponsesRequest();
        request.setResponses(answers);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));

        SessionAlreadyActiveException ex = assertThrows(
                SessionAlreadyActiveException.class,
                () -> sessionService.submitResponses(1L, request));

        assertTrue(ex.getMessage().contains("exactly 6 questions"));
        verify(userResponseRepository, never()).save(any(UserResponse.class));
    }

    @Test
    void submitResponses_duplicateKeySubmittedTwice_stillRejectedAsMissingRequired() {
        // 6 answers submitted, but "budget" sent twice and "usage" missing
        List<SubmitResponsesRequest.QuestionAnswer> answers = new ArrayList<>();
        answers.add(qa("budget", "600K_TO_1M"));
        answers.add(qa("budget", "ABOVE_1M")); // duplicate key
        answers.add(qa("fuel_type", "PETROL"));
        answers.add(qa("car_type", "HATCHBACK"));
        answers.add(qa("seating", "5"));
        answers.add(qa("transmission", "MANUAL"));
        // "usage" never provided

        SubmitResponsesRequest request = new SubmitResponsesRequest();
        request.setResponses(answers);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));

        SessionAlreadyActiveException ex = assertThrows(
                SessionAlreadyActiveException.class,
                () -> sessionService.submitResponses(1L, request));

        assertTrue(ex.getMessage().contains("usage"));
    }

    @Test
    void submitResponses_completedSession_throwsException() {
        session.setStatus(SessionStatus.COMPLETED);

        List<SubmitResponsesRequest.QuestionAnswer> answers = new ArrayList<>();
        answers.add(qa("budget", "600K_TO_1M"));
        answers.add(qa("fuel_type", "PETROL"));
        answers.add(qa("car_type", "HATCHBACK"));
        answers.add(qa("seating", "5"));
        answers.add(qa("usage", "CITY"));
        answers.add(qa("transmission", "MANUAL"));

        SubmitResponsesRequest request = new SubmitResponsesRequest();
        request.setResponses(answers);

        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.of(session));

        assertThrows(SessionAlreadyActiveException.class,
                () -> sessionService.submitResponses(1L, request));
    }

    @Test
    void submitResponses_sessionNotFoundOrWrongOwner_throwsException() {
        List<SubmitResponsesRequest.QuestionAnswer> answers = new ArrayList<>();
        answers.add(qa("budget", "600K_TO_1M"));
        answers.add(qa("fuel_type", "PETROL"));
        answers.add(qa("car_type", "HATCHBACK"));
        answers.add(qa("seating", "5"));
        answers.add(qa("usage", "CITY"));
        answers.add(qa("transmission", "MANUAL"));

        SubmitResponsesRequest request = new SubmitResponsesRequest();
        request.setResponses(answers);

        // findByIdAndUserId returns empty — either wrong session or wrong owner
        when(sessionRepository.findByIdAndUserId(1L, null))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> sessionService.submitResponses(1L, request));
    }
}