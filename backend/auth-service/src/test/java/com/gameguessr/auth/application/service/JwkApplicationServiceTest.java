package com.gameguessr.auth.application.service;

import com.gameguessr.auth.application.rest.dto.JwkSetResponse;
import com.gameguessr.auth.infrastructure.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwkApplicationService")
class JwkApplicationServiceTest {

    private JwkApplicationService jwkApplicationService;

    private static final String RSA_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx9CZBhI2dE1HjD7gsrHT\nDG3KKa/f9r5LHrtDK0TcdEOt+EqoBD7G4lL0SA1UhTj3Qb8kRpoLAXEDwfF+QG8C\nv2VN5Oll9nDAVdhO5Agj4X/KTuTiMGpkpwrQhNeOJ0qLIRLo2ySzTP/zfn8Hxrsv\nOuL8u8qgPgZ5HaQyEFvT1idlJ0/97DXhfl0XSgCvqXiOwCZ+6DasODuCwEvYD6A7\nyaaNw+io8e9Wu1PuvkEpRv2hlWCljMqFCWyqicd+sQTHFVV5/8UVKLStP012anNh\nkySXkbOg3mxhgUmjhJcsndigmxX1l84oVUauUTKd6sd58WYu298lfcILeGgD1zV5g\nQIDAQAB\n-----END PUBLIC KEY-----";

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        ReflectionTestUtils.setField(jwtProperties, "rsaPublicKey", RSA_PUBLIC_KEY);

        jwkApplicationService = new JwkApplicationService(jwtProperties);
    }

    @Test
    @DisplayName("getJwkSet — returns JwkSetResponse with valid structure")
    void getJwkSet_returnsValidStructure() {
        JwkSetResponse response = jwkApplicationService.getJwkSet();

        assertThat(response).isNotNull();
        assertThat(response.getKeys()).isNotNull();
        assertThat(response.getKeys()).hasSize(1);
    }

    @Test
    @DisplayName("getJwkSet — Jwk contains correct key type")
    void getJwkSet_containsCorrectKeyType() {
        JwkSetResponse response = jwkApplicationService.getJwkSet();

        JwkSetResponse.Jwk jwk = response.getKeys().get(0);
        assertThat(jwk.getKty()).isEqualTo("RSA");
        assertThat(jwk.getUse()).isEqualTo("sig");
        assertThat(jwk.getAlg()).isEqualTo("RS256");
    }

    @Test
    @DisplayName("getJwkSet — Jwk contains non-null modulus and exponent")
    void getJwkSet_containsNonNullModulusAndExponent() {
        JwkSetResponse response = jwkApplicationService.getJwkSet();

        JwkSetResponse.Jwk jwk = response.getKeys().get(0);
        assertThat(jwk.getN()).isNotNull();
        assertThat(jwk.getE()).isNotNull();
        assertThat(jwk.getN()).isNotEmpty();
        assertThat(jwk.getE()).isNotEmpty();
    }

    @Test
    @DisplayName("getJwkSet — kid is deterministic based on key")
    void getJwkSet_kidIsDeterministic() {
        JwkSetResponse response1 = jwkApplicationService.getJwkSet();
        JwkSetResponse response2 = jwkApplicationService.getJwkSet();

        String kid1 = response1.getKeys().get(0).getKid();
        String kid2 = response2.getKeys().get(0).getKid();

        assertThat(kid1).isEqualTo(kid2);
    }

    @Test
    @DisplayName("getJwkSet — kid is 16 characters long (first 16 of SHA-256)")
    void getJwkSet_kidHasCorrectLength() {
        JwkSetResponse response = jwkApplicationService.getJwkSet();

        String kid = response.getKeys().get(0).getKid();
        assertThat(kid).hasSize(16);
    }
}
