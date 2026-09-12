package com.carmatch.service;

import com.carmatch.entity.RefreshToken;
import com.carmatch.entity.User;
import com.carmatch.exception.TokenRefreshException;
import com.carmatch.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    // Creates and saves a brand-new refresh token for this user.
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        // UUID gives us a long, random, hard-to-guess string —
        // this is NOT a JWT, just an opaque random ID stored in our DB.
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        return refreshTokenRepository.save(refreshToken);
    }

    // Looks up a refresh token by its string value. If it doesn't exist
    // at all (never issued, or already logged out/rotated away), fail fast.
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException(
                        "Refresh token not found. Please log in again."));
    }

    // Throws if the token has expired, and cleans it up from the DB either way.
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(
                    "Refresh token expired. Please log in again.");
        }
        return token;
    }

    // Rotation: the old token is destroyed the moment it's used,
    // and a fresh one takes its place. If someone steals an old
    // (already-used) refresh token later, it simply won't exist anymore.
    public RefreshToken rotateToken(RefreshToken oldToken) {
        User user = oldToken.getUser();
        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(user);
    }

    // Logout = revoke just this one device/session's refresh token.
    // Other devices where the user is still logged in remain unaffected.
    public void revokeToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}