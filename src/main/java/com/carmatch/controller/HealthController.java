package com.carmatch.controller;

import com.carmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {


    @Autowired
    private UserRepository userRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();

        try {

            long userCount = userRepository.count();

            response.put("status", "UP");
            response.put("message", "CarMatch API is running");
            response.put("database", "connected");
            response.put("userCount", userCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("status", "DEGRADED");
            response.put("message", "API is running but database is unreachable");
            response.put("database", "disconnected");

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}