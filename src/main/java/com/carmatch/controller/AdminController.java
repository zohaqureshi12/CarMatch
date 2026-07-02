package com.carmatch.controller;

import com.carmatch.dto.response.ApiResponse;
import com.carmatch.dto.response.UserProfileResponse;
import com.carmatch.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Users", description = "Admin user management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserProfileResponse> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single user detail")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(
            @PathVariable Long id) {

        UserProfileResponse user = adminService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", user));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<ApiResponse<UserProfileResponse>> deactivateUser(
            @PathVariable Long id) {

        UserProfileResponse user = adminService.deactivateUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User deactivated successfully", user));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Reactivate a user account")
    public ResponseEntity<ApiResponse<UserProfileResponse>> activateUser(
            @PathVariable Long id) {

        UserProfileResponse user = adminService.activateUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("User activated successfully", user));
    }
}