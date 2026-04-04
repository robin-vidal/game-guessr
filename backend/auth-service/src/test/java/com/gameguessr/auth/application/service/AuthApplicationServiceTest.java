package com.gameguessr.auth.application.service;

import com.gameguessr.auth.domain.model.LoginResult;
import com.gameguessr.auth.domain.model.User;
import com.gameguessr.auth.domain.port.outbound.TokenBlacklist;
import com.gameguessr.auth.domain.port.outbound.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApplicationService")
class AuthApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.gameguessr.auth.domain.port.outbound.TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlacklist tokenBlacklist;

    @InjectMocks
    private AuthApplicationService authService;

    private static final String USERNAME = "testuser";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPassword";
    private static final String TOKEN = "jwt.token.here";

    // ── register ───────────────────────────────────────────────────

    @Test
    @DisplayName("register — succeeds with new username")
    void register_newUsername_succeeds() {
        User createdUser = User.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .build();

        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        User result = authService.register(USERNAME, RAW_PASSWORD);

        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(result.getId()).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register — throws when username already exists")
    void register_existingUsername_throws() {
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(USERNAME, RAW_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register — encodes password before saving")
    void register_encodesPassword() {
        User createdUser = User.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .build();

        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(createdUser);

        authService.register(USERNAME, RAW_PASSWORD);

        verify(passwordEncoder).encode(RAW_PASSWORD);
    }

    // ── login ────────────────────────────────────────────────────────

    @Test
    @DisplayName("login — returns LoginResult for valid credentials")
    void login_validCredentials_returnsLoginResult() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .build();
        String expectedToken = "jwt.token.here";

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenService.generateToken(userId, USERNAME)).thenReturn(expectedToken);

        LoginResult result = authService.login(USERNAME, RAW_PASSWORD);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getToken()).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("login — throws when user not found")
    void login_userNotFound_throws() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(USERNAME, RAW_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");

        verify(tokenService, never()).generateToken(any(UUID.class), any());
    }

    @Test
    @DisplayName("login — throws when password does not match")
    void login_wrongPassword_throws() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .build();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(USERNAME, RAW_PASSWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");

        verify(tokenService, never()).generateToken(any(UUID.class), any());
    }

    @Test
    @DisplayName("login — encodes password and compares")
    void login_comparesPasswords() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .build();
        String token = "jwt.token.here";

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenService.generateToken(userId, USERNAME)).thenReturn(token);

        authService.login(USERNAME, RAW_PASSWORD);

        verify(passwordEncoder).matches(RAW_PASSWORD, ENCODED_PASSWORD);
    }

    // ── logout ──────────────────────────────────────────────────────

    @Test
    @DisplayName("logout — invalidates token via blacklist")
    void logout_invalidatesToken() {
        authService.logout(TOKEN);

        verify(tokenBlacklist).invalidate(TOKEN);
    }

    @Test
    @DisplayName("logout — calls blacklist with correct token")
    void logout_callsBlacklistWithCorrectToken() {
        String token = "another.jwt.token";

        authService.logout(token);

        verify(tokenBlacklist).invalidate(token);
    }
}