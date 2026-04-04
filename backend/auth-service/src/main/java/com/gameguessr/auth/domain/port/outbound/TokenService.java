package com.gameguessr.auth.domain.port.outbound;

import com.gameguessr.auth.domain.model.JwtTokenInfo;

import java.util.UUID;

public interface TokenService {

    String generateToken(UUID userId, String username);

    String extractUsername(String token);

    UUID extractUserId(String token);

    JwtTokenInfo parseToken(String token);
}