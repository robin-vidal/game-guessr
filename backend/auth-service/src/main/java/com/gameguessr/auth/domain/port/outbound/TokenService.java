package com.gameguessr.auth.domain.port.outbound;

public interface TokenService {

    String generateToken(String username);

    String extractUsername(String token);

    boolean isTokenValid(String token);
}