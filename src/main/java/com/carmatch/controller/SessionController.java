package com.carmatch.controller;

import com.carmatch.dto.request.SubmitResponsesRequest;
import com.carmatch.dto.response.ApiResponse;
import com.carmatch.dto.response.SessionResponse;
import com.carmatch.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Questionnaire", description = "Session and questionnaire endpoints")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping("/questions")
    @Operation(summary = "Get all 6 questionnaire questions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getQuestions() {
        List<Map<String, Object>> questions = sessionService.getQuestions();
        return ResponseEntity.ok(
                ApiResponse.success("Questions retrieved successfully", questions));
    }

    @PostMapping("/start")
    @Operation(summary = "Start a new recommendation session")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SessionResponse>> startSession() {
        SessionResponse response = sessionService.startSession();
        return ResponseEntity.ok(
                ApiResponse.success("Session started successfully", response));
    }

    @PostMapping("/{sessionId}/responses")
    @Operation(summary = "Submit answers to all 6 questions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SessionResponse>> submitResponses(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitResponsesRequest request) {
        SessionResponse response = sessionService.submitResponses(sessionId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Responses submitted successfully", response));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get session details")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @PathVariable Long sessionId) {
        SessionResponse response = sessionService.getSession(sessionId);
        return ResponseEntity.ok(
                ApiResponse.success("Session retrieved successfully", response));
    }
}