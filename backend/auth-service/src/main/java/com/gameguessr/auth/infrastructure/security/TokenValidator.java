package com.gameguessr.auth.infrastructure.security;

public interface TokenValidator {

    boolean isInvalidated(String token);

    void cleanExpired();
}
