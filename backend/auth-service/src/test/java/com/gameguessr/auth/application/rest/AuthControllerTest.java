package com.gameguessr.auth.application.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameguessr.auth.application.rest.dto.LoginRequest;
import com.gameguessr.auth.application.rest.dto.RegisterRequest;
import com.gameguessr.auth.domain.model.User;
import com.gameguessr.auth.domain.port.inbound.AuthUseCase;
import com.gameguessr.auth.domain.port.outbound.TokenService;
import com.gameguessr.auth.infrastructure.security.TokenValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenValidator tokenValidator;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";

    // ── POST /register ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /register — 201 on successful registration")
    void register_validRequest_returns201() throws Exception {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .password("encodedPassword")
                .build();

        when(authUseCase.register(USERNAME, PASSWORD)).thenReturn(user);

        RegisterRequest req = new RegisterRequest();
        req.setUsername(USERNAME);
        req.setPassword(PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /register — 400 when username is blank")
    void register_blankUsername_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("");
        req.setPassword(PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register — 400 when password is blank")
    void register_blankPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(USERNAME);
        req.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register — 400 when username already exists")
    void register_existingUsername_returns400() throws Exception {
        when(authUseCase.register(USERNAME, PASSWORD))
                .thenThrow(new IllegalArgumentException("Username already exists: " + USERNAME));

        RegisterRequest req = new RegisterRequest();
        req.setUsername(USERNAME);
        req.setPassword(PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /login ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /login — 200 with token on valid credentials")
    void login_validCredentials_returns200() throws Exception {
        String token = "jwt.token.here";
        when(authUseCase.login(USERNAME, PASSWORD)).thenReturn(token);

        LoginRequest req = new LoginRequest();
        req.setUsername(USERNAME);
        req.setPassword(PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    @DisplayName("POST /login — 400 when username is blank")
    void login_blankUsername_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("");
        req.setPassword(PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login — 400 when password is blank")
    void login_blankPassword_returns400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(USERNAME);
        req.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /login — 400 for invalid credentials")
    void login_invalidCredentials_returns400() throws Exception {
        when(authUseCase.login(USERNAME, PASSWORD))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        LoginRequest req = new LoginRequest();
        req.setUsername(USERNAME);
        req.setPassword(PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /logout ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /logout — 204 with valid token")
    void logout_validToken_returns204() throws Exception {
        String token = "valid.jwt.token";

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(authUseCase).logout(token);
    }

    @Test
    @DisplayName("POST /logout — 401 with missing authorization header")
    void logout_missingAuthHeader_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());

        verify(authUseCase, never()).logout(any());
    }

    @Test
    @DisplayName("POST /logout — 401 with invalid authorization format")
    void logout_invalidAuthFormat_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isUnauthorized());

        verify(authUseCase, never()).logout(any());
    }
}