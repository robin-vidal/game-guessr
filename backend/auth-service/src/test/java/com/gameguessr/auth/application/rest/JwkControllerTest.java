package com.gameguessr.auth.application.rest;

import com.gameguessr.auth.application.rest.dto.JwkSetResponse;
import com.gameguessr.auth.domain.port.inbound.JwkUseCase;
import com.gameguessr.auth.domain.port.outbound.TokenService;
import com.gameguessr.auth.infrastructure.security.TokenValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JwkController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("JwkController")
class JwkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwkUseCase jwkUseCase;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenValidator tokenValidator;

    @Test
    @DisplayName("GET /.well-known/jwks.json — returns 200 with JwkSet")
    void getJwkSet_returns200() throws Exception {
        JwkSetResponse.Jwk jwk = JwkSetResponse.Jwk.builder()
                .kty("RSA")
                .use("sig")
                .kid("test-kid-12345")
                .alg("RS256")
                .n("test-modulus")
                .e("AQAB")
                .build();

        JwkSetResponse response = JwkSetResponse.builder()
                .keys(java.util.List.of(jwk))
                .build();

        when(jwkUseCase.getJwkSet()).thenReturn(response);

        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].use").value("sig"))
                .andExpect(jsonPath("$.keys[0].kid").value("test-kid-12345"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
                .andExpect(jsonPath("$.keys[0].n").value("test-modulus"))
                .andExpect(jsonPath("$.keys[0].e").value("AQAB"));
    }

    @Test
    @DisplayName("GET /.well-known/jwks.json — delegates to JwkUseCase")
    void getJwkSet_delegatesToUseCase() throws Exception {
        when(jwkUseCase.getJwkSet()).thenReturn(JwkSetResponse.builder()
                .keys(java.util.List.of())
                .build());

        mockMvc.perform(get("/.well-known/jwks.json"));

        verify(jwkUseCase).getJwkSet();
    }
}
