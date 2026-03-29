package com.gameguessr.auth.infrastructure.security;

import com.gameguessr.auth.infrastructure.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenService")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final Long EXPIRATION = 86400000L;
    private static final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        ReflectionTestUtils.setField(properties, "secret", SECRET);
        ReflectionTestUtils.setField(properties, "expiration", EXPIRATION);

        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    @DisplayName("generateToken — creates valid token for username")
    void generateToken_createsValidToken() {
        String token = jwtTokenService.generateToken(USERNAME);

        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUsername — extracts username from valid token")
    void extractUsername_extractsFromValidToken() {
        String token = jwtTokenService.generateToken(USERNAME);

        String extractedUsername = jwtTokenService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("generateToken — same username produces different tokens (timestamps)")
    void generateToken_sameUsername_producesDifferentTokens() throws InterruptedException {
        String token1 = jwtTokenService.generateToken(USERNAME);
        Thread.sleep(1000);
        String token2 = jwtTokenService.generateToken(USERNAME);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("extractUsername — same username from different tokens")
    void extractUsername_differentTokens_sameUsername() {
        String token1 = jwtTokenService.generateToken(USERNAME);
        String token2 = jwtTokenService.generateToken(USERNAME);

        String username1 = jwtTokenService.extractUsername(token1);
        String username2 = jwtTokenService.extractUsername(token2);

        assertThat(username1).isEqualTo(username2);
        assertThat(username1).isEqualTo(USERNAME);
    }
}