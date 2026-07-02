package com.carmatch.controller;

import com.carmatch.dto.response.ApiResponse;
import com.carmatch.dto.response.SessionResponse;
import com.carmatch.dto.response.SuggestionResponse;
import com.carmatch.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "History", description = "Session and suggestion history endpoints")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping("/sessions")
    @Operation(summary = "Get all sessions for current user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getAllSessions() {
        List<SessionResponse> sessions = historyService.getAllSessions();
        return ResponseEntity.ok(
                ApiResponse.success("Sessions retrieved successfully", sessions));
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get full detail of one session")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionDetail(
            @PathVariable Long sessionId) {
        Map<String, Object> detail = historyService.getSessionDetail(sessionId);
        return ResponseEntity.ok(
                ApiResponse.success("Session detail retrieved successfully", detail));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get all suggestions across all sessions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SuggestionResponse>>> getAllSuggestions() {
        List<SuggestionResponse> suggestions = historyService.getAllSuggestions();
        return ResponseEntity.ok(
                ApiResponse.success("Suggestions retrieved successfully", suggestions));
    }
}