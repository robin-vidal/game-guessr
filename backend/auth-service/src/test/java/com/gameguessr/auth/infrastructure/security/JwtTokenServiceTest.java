package com.gameguessr.auth.infrastructure.security;

import com.gameguessr.auth.domain.port.outbound.TokenService;
import com.gameguessr.auth.infrastructure.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenService")
class JwtTokenServiceTest {

    private TokenService tokenService;

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final Long EXPIRATION = 86400000L;
    private static final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        ReflectionTestUtils.setField(properties, "secret", SECRET);
        ReflectionTestUtils.setField(properties, "expiration", EXPIRATION);

        tokenService = new JwtTokenService(properties);
    }

    @Test
    @DisplayName("generateToken — creates valid token for username")
    void generateToken_createsValidToken() {
        String token = tokenService.generateToken(USERNAME);

        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUsername — extracts username from valid token")
    void extractUsername_extractsFromValidToken() {
        String token = tokenService.generateToken(USERNAME);

        String extractedUsername = tokenService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("isTokenValid — returns true for valid token")
    void isTokenValid_validToken_returnsTrue() {
        String token = tokenService.generateToken(USERNAME);

        assertThat(tokenService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid — returns false for invalid token")
    void isTokenValid_invalidToken_returnsFalse() {
        String invalidToken = "invalid.token.here";

        assertThat(tokenService.isTokenValid(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid — returns false for tampered token")
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = tokenService.generateToken(USERNAME);
        String tamperedToken = token + "tampered";

        assertThat(tokenService.isTokenValid(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("generateToken — same username produces different tokens (timestamps)")
    void generateToken_sameUsername_producesDifferentTokens() throws InterruptedException {
        String token1 = tokenService.generateToken(USERNAME);
        Thread.sleep(1000);
        String token2 = tokenService.generateToken(USERNAME);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("extractUsername — same username from different tokens")
    void extractUsername_differentTokens_sameUsername() {
        String token1 = tokenService.generateToken(USERNAME);
        String token2 = tokenService.generateToken(USERNAME);

        String username1 = tokenService.extractUsername(token1);
        String username2 = tokenService.extractUsername(token2);

        assertThat(username1).isEqualTo(username2);
        assertThat(username1).isEqualTo(USERNAME);
    }
}