package com.carmatch.controller;

import com.carmatch.dto.response.ApiResponse;
import com.carmatch.dto.response.RecommendationResponse;
import com.carmatch.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recommendations", description = "Car recommendation engine")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/{sessionId}/recommendations")
    @Operation(summary = "Get top 20 car recommendations for this session")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(
            @PathVariable Long sessionId) {

        List<RecommendationResponse> recommendations =
                recommendationService.getRecommendations(sessionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Recommendations retrieved successfully", recommendations));
    }
}