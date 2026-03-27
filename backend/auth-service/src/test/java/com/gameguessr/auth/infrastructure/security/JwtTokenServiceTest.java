package com.gameguessr.auth.infrastructure.security;

import com.gameguessr.auth.infrastructure.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenService")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    private static final String RSA_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDH0JkGEjZ0TUeM\nPuCysdMMbcopr9/2vkseu0MrRNx0Q634SqgEPsbiUvRIDVSFOPdBvyRGmgsBcQPB\n8X5AbwK/ZU3k6WX2cMBV2E7kCCPhf8pO5OIwamSnCtCE144nSoshEujbJLNM//N+\nfwfGuy864vy7yqA+BnkdpDIQW9PWJ2UnT/3sNeF+XRdKAK+peI7AJn7oNqw4O4LA\nS9gPoDvJpo3D6Kjx71a7U+6+QSlG/aGVYKWMyoUJbKqJx36xBMcVVXn/xRUotK0/\nTXZqc2GTJJeRs6DebGGBSaOElyyd2KCbFfWXzihVRq5RMp3qx3nxZi7b3yV9wgt4\naAPXNXmBAgMBAAECggEAMDzqLF6KOeKMMs3dNhaa8kWxCAZXBbzj6rvfl9sMc58B\nKU9wp3G2Vt4a7igVrwHVI1CvQ8IdhRARDamsxoZmPemJeAlmnauTq0XIqeFTNDXD\nE+/KsVCGExOXynI1dcPXubjUcU83YA/say3kpTP8T3WL+RQOQlsSsza4BV390pWL\nZnQC0/KUcYg+r/3k16lgzTk3YOko0Eq9KcXMjlQSxcO/NbmbagJj0bFulunR1Den\nLCZmTeyiq4xGzyPqG6YYdPQUWdNdhjm9NZIB3FWh2oe74r3gR7b0JJWQibtHyAdD\n5mLNPxoG8cnNgHsPIUR2k/w6CzTuTtIB8ctFLj2WSQKBgQD50AQu1tK+f8A82iRn\ncE4/aeLFKhA23q2YT1eL8xg7t3h1gXeNEatNm8gQl3PbQcy2MKr+HBQdw6KhTpBR\nglmBYwS2XmjO2JQYOWAlaoMKQAGc7QXglzVyt0NehCm7tZ2rw6PFx1QyvCeFU/5G\nPkhhW2fAEp57KcriKHl6JtxwFQKBgQDMw4+2x/T3IJzuPPJ59AW58uAd3aD5Pv5C\nAHvw+xFfB2txj2xwRY8Xe2pOCMOqVFrDm9PMkb76QOEYeYXoJ7t903HKsdQmgjvr\nj0OC2K1LSrq8yy0PG7F8fUj4wyvHwlfGatCdNl5nOH7rd1eS4ZEqAUxRM36IA11/\ntAVedFBSvQKBgCb2yRA9NZW8+1ECfVcc7zd43oP+MZU9Wjo9ddxASHFYBpPdY7Y/\nIUbzm1rrJIj4VGO+Z41G9+RElWMvi9cKy8cgmKWRSCurID4/BL4RROseXKILXjVP\noGiKKRne51t90l/uHedFVd6r9kYXebufD08QILiWrt1rAVgRFBF3aj6ZAoGBAMdh\nbOCnl0nDAcugoOJK0ACeE8hA8+t+gQk3e3fMoL+Nc/thkk9IC+rrizlso0mJVc++\nPd+l0vXOkt/IBLFnbtM4PkcGHPNaQIpAN5aC8Uqs+2O12qwpZQs42wMO1+Rswhc8\nDuHl/Mo5YqxYyncFGNpz5SH0KDK8RCCC4+9zBntpAoGAdhxtJR3oUtwk/i3JPfp1\nWVAiz4EQmOjBAALyQ5MdmwuLmSOg+XIgJ3fM2L9bFIbnxGIOJQcyDEVednUQp7xM\nxkNGBTegV8s2vGY8DzaF4iW/1l6V/eRa/zvDTsAleBpAyULEOxV5mnt9qD5+OZfU\nlj6J529k5V8o3mJO4jgw3yw=\n-----END PRIVATE KEY-----";

    private static final String RSA_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx9CZBhI2dE1HjD7gsrHT\nDG3KKa/f9r5LHrtDK0TcdEOt+EqoBD7G4lL0SA1UhTj3Qb8kRpoLAXEDwfF+QG8C\nv2VN5Oll9nDAVdhO5Agj4X/KTuTiMGpkpwrQhNeOJ0qLIRLo2ySzTP/zfn8Hxrsv\nOuL8u8qgPgZ5HaQyEFvT1idlJ0/97DXhfl0XSgCvqXiOwCZ+6DasODuCwEvYD6A7\nyaaNw+io8e9Wu1PuvkEpRv2hlWCljMqFCWyqicd+sQTHFVV5/8UVKLStP012anNh\nkySXkbOg3mxhgUmjhJcsndigmxX1l84oVUauUTKd6sd58WYu298lfcILeGgD1zV5g\nQIDAQAB\n-----END PUBLIC KEY-----";

    private static final Long EXPIRATION = 86400000L;
    private static final String USERNAME = "testuser";
    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        JwtProperties properties = new JwtProperties();
        ReflectionTestUtils.setField(properties, "rsaPrivateKey", RSA_PRIVATE_KEY);
        ReflectionTestUtils.setField(properties, "rsaPublicKey", RSA_PUBLIC_KEY);
        ReflectionTestUtils.setField(properties, "expiration", EXPIRATION);

        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    @DisplayName("generateToken — creates valid token for username")
    void generateToken_createsValidToken() {
        String token = jwtTokenService.generateToken(USER_ID, USERNAME);

        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUsername — extracts username from valid token")
    void extractUsername_extractsFromValidToken() {
        String token = jwtTokenService.generateToken(USER_ID, USERNAME);

        String extractedUsername = jwtTokenService.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("extractUserId — extracts userId from valid token")
    void extractUserId_extractsFromValidToken() {
        String token = jwtTokenService.generateToken(USER_ID, USERNAME);

        UUID extractedUserId = jwtTokenService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("generateToken — same username produces different tokens (timestamps)")
    void generateToken_sameUsername_producesDifferentTokens() throws InterruptedException {
        String token1 = jwtTokenService.generateToken(USER_ID, USERNAME);
        Thread.sleep(1000);
        String token2 = jwtTokenService.generateToken(USER_ID, USERNAME);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("extractUsername — same username from different tokens")
    void extractUsername_differentTokens_sameUsername() {
        String token1 = jwtTokenService.generateToken(USER_ID, USERNAME);
        String token2 = jwtTokenService.generateToken(USER_ID, USERNAME);

        String username1 = jwtTokenService.extractUsername(token1);
        String username2 = jwtTokenService.extractUsername(token2);

        assertThat(username1).isEqualTo(username2);
        assertThat(username1).isEqualTo(USERNAME);
    }
}
