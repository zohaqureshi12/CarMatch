package com.carmatch.service;

import com.carmatch.dto.request.LoginRequest;
import com.carmatch.dto.request.RegisterRequest;
import com.carmatch.dto.response.AuthResponse;
import com.carmatch.entity.User;
import com.carmatch.enums.Role;
import com.carmatch.exception.InvalidCredentialsException;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.exception.UserAlreadyExistsException;
import com.carmatch.repository.UserRepository;
import com.carmatch.security.CustomUserDetailsService;
import com.carmatch.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.carmatch.dto.request.ResendOtpRequest;
import com.carmatch.dto.request.VerifyOtpRequest;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);
        user.setIsEmailVerified(false);

        // Generate 6-digit OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        // Send OTP email
        emailService.sendOtpEmail(user.getEmail(), otp);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getId(),
                user.getRole().name(),
                user.getName()
        );
    }

    private String generateOtp() {
        int otp = 100000 + new java.util.Random().nextInt(900000);
        return String.valueOf(otp);
    }
    public String verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

        if (user.getIsEmailVerified()) {
            return "Email is already verified";
        }

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new InvalidCredentialsException("Invalid OTP");
        }

        if (user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new InvalidCredentialsException(
                    "OTP has expired. Please request a new one.");
        }

        user.setIsEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return "Email verified successfully";
    }

    public String resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

        if (user.getIsEmailVerified()) {
            return "Email is already verified";
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return "OTP resent successfully";
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password"));

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException(
                    "Your account has been deactivated");
        }

        if (!user.getIsEmailVerified()) {
            throw new InvalidCredentialsException(
                    "Please verify your email before logging in. Check your inbox for the OTP.");
        }

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(
                token,
                user.getId(),
                user.getRole().name(),
                user.getName()
        );
    }
}