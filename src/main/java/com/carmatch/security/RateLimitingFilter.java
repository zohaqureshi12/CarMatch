package com.carmatch.security;

import com.carmatch.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // One bucket per (client IP + endpoint) combination.
    // ConcurrentHashMap is thread-safe, since multiple requests can
    // arrive at the same time from different users.
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        Bandwidth limit = resolveLimit(path);

        // Not one of our rate-limited auth endpoints — pass straight through.
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        String bucketKey = clientIp + ":" + path;

        Bucket bucket = buckets.computeIfAbsent(bucketKey,
                key -> Bucket.builder().addLimit(limit).build());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            sendTooManyRequests(response);
        }
    }

    // Maps each auth endpoint to its own rate limit. Returns null for
    // any path that shouldn't be rate-limited at all.
    private Bandwidth resolveLimit(String path) {
        return switch (path) {
            case "/api/auth/login" ->
                    Bandwidth.builder().capacity(5)
                            .refillGreedy(5, Duration.ofMinutes(1)).build();
            case "/api/auth/register" ->
                    Bandwidth.builder().capacity(3)
                            .refillGreedy(3, Duration.ofHours(1)).build();
            case "/api/auth/resend-otp" ->
                    Bandwidth.builder().capacity(3)
                            .refillGreedy(3, Duration.ofMinutes(10)).build();
            case "/api/auth/verify-otp" ->
                    Bandwidth.builder().capacity(10)
                            .refillGreedy(10, Duration.ofMinutes(10)).build();
            case "/api/auth/refresh" ->
                    Bandwidth.builder().capacity(20)
                            .refillGreedy(20, Duration.ofMinutes(1)).build();
            default -> null;
        };
    }

    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("application/json");
        ApiResponse<Object> body = ApiResponse.error(
                "Too many requests. Please wait a moment and try again.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}