package com.carmatch.service;

import com.carmatch.dto.request.LoginRequest;
import com.carmatch.dto.request.RegisterRequest;
import com.carmatch.dto.response.AuthResponse;
import com.carmatch.entity.User;
import com.carmatch.enums.Role;
import com.carmatch.exception.InvalidCredentialsException;
import com.carmatch.exception.UserAlreadyExistsException;
import com.carmatch.repository.UserRepository;
import com.carmatch.security.CustomUserDetailsService;
import com.carmatch.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setName("Zoa");
        existingUser.setEmail("zoa@gmail.com");
        existingUser.setPassword("hashedPassword123");
        existingUser.setRole(Role.USER);
        existingUser.setIsActive(true);
    }

    //  Register Tests

    @Test
    void register_newEmail_createsUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Zoa");
        request.setEmail("zoa@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("zoa@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword123");
        when(userDetailsService.loadUserByUsername("zoa@gmail.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Zoa", response.getName());
        assertEquals("USER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Zoa");
        request.setEmail("zoa@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("zoa@gmail.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request));

        // Should never attempt to save if email already exists
        verify(userRepository, never()).save(any(User.class));
    }

    // ── Login Tests ───────────────────────────────────────────────

    @Test
    void login_correctCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("zoa@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("zoa@gmail.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashedPassword123"))
                .thenReturn(true);
        when(userDetailsService.loadUserByUsername("zoa@gmail.com"))
                .thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Zoa", response.getName());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("zoa@gmail.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("zoa@gmail.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword123"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void login_emailNotFound_throwsInvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nobody@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }

    @Test
    void login_deactivatedAccount_throwsInvalidCredentials() {
        existingUser.setIsActive(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("zoa@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("zoa@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        InvalidCredentialsException ex = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request));

        assertTrue(ex.getMessage().contains("deactivated"));
    }
}