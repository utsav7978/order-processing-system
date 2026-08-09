package com.orderplatform.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET);

    private String buildToken(String email, Long userId, String role, long expiryOffsetMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryOffsetMs))
                .signWith(key)
                .compact();
    }

    @Test
    void isTokenValid_returnsTrue_forWellFormedUnexpiredToken() {
        String token = buildToken("jane@example.com", 5L, "USER", 60_000);
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forExpiredToken() {
        String token = buildToken("jane@example.com", 5L, "USER", -60_000);
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forGarbageToken() {
        assertThat(jwtUtil.isTokenValid("not-a-real-token")).isFalse();
    }

    @Test
    void extractUserId_returnsUserIdClaim() {
        String token = buildToken("jane@example.com", 42L, "ADMIN", 60_000);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void extractRole_returnsRoleClaim() {
        String token = buildToken("jane@example.com", 5L, "ADMIN", 60_000);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void extractEmail_returnsSubjectClaim() {
        String token = buildToken("jane@example.com", 5L, "USER", 60_000);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("jane@example.com");
    }
}
