package com.carmatch.service;

import com.carmatch.dto.response.UserProfileResponse;
import com.carmatch.entity.User;
import com.carmatch.exception.ResourceNotFoundException;
import com.carmatch.repository.SessionRepository;
import com.carmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private UserProfileResponse mapToResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    // ── List All Users (Paginated) ──────────────────────────────
    public Page<UserProfileResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // ── Get Single User Detail ──────────────────────────────────
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return mapToResponse(user);
    }

    // ── Deactivate User ──────────────────────────────────────────
    public UserProfileResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        if (user.getRole() == com.carmatch.enums.Role.ADMIN) {
            throw new com.carmatch.exception.UnauthorizedException(
                    "Cannot deactivate an admin account");
        }

        user.setIsActive(false);
        userRepository.save(user);
        return mapToResponse(user);
    }

    // ── Activate User ────────────────────────────────────────────
    public UserProfileResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        user.setIsActive(true);
        userRepository.save(user);
        return mapToResponse(user);
    }
}