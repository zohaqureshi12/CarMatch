package com.carmatch.controller;

import com.carmatch.dto.request.ChangePasswordRequest;
import com.carmatch.dto.request.UpdateProfileRequest;
import com.carmatch.dto.response.ApiResponse;
import com.carmatch.dto.response.UserProfileResponse;
import com.carmatch.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "View and update user profile")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        UserProfileResponse response = userService.getProfile();
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update name and email")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", response));
    }

    @PatchMapping("/me/change-password")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully", null));
    }
}