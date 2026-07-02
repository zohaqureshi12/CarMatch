package com.carmatch.controller;

import com.carmatch.dto.response.ApiResponse;
import com.carmatch.dto.response.SuggestionResponse;
import com.carmatch.service.SuggestionService;
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
@Tag(name = "Suggestions", description = "Best match suggestion endpoints")
public class SuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    @GetMapping("/{sessionId}/suggestion")
    @Operation(summary = "Get best match suggestion for completed session")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SuggestionResponse>>> getSuggestions(
            @PathVariable Long sessionId) {

        List<SuggestionResponse> suggestions =
                suggestionService.getSuggestions(sessionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Best match suggestions retrieved successfully",
                        suggestions));
    }
}