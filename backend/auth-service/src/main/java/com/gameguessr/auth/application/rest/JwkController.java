package com.gameguessr.auth.application.rest;

import com.gameguessr.auth.application.rest.dto.JwkSetResponse;
import com.gameguessr.auth.domain.port.inbound.JwkUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
@Tag(name = "JWKS", description = "JSON Web Key Set endpoint")
public class JwkController {

    private final JwkUseCase jwkUseCase;

    @GetMapping(value = "/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get JWKS", description = "Returns the public key in JWKS format")
    @ApiResponse(responseCode = "200", description = "JWKS retrieved successfully")
    public JwkSetResponse getJwkSet() {
        return jwkUseCase.getJwkSet();
    }
}
