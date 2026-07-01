package com.carmatch.controller;

import com.carmatch.dto.request.SwipeRequest;
import com.carmatch.dto.response.ApiResponse;
import com.carmatch.dto.response.SwipeResponse;
import com.carmatch.service.SwipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Swipes", description = "Swipe session endpoints")
public class SwipeController {

    @Autowired
    private SwipeService swipeService;

    @PostMapping("/{sessionId}/swipe")
    @Operation(summary = "Record a LIKE or PASS on a car")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SwipeResponse>> recordSwipe(
            @PathVariable Long sessionId,
            @Valid @RequestBody SwipeRequest request) {

        SwipeResponse response = swipeService.recordSwipe(sessionId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Swipe recorded successfully", response));
    }

    @GetMapping("/{sessionId}/swipes")
    @Operation(summary = "Get all swipes for a session")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SwipeResponse>>> getSwipes(
            @PathVariable Long sessionId) {

        List<SwipeResponse> swipes = swipeService.getSwipes(sessionId);
        return ResponseEntity.ok(
                ApiResponse.success("Swipes retrieved successfully", swipes));
    }
}