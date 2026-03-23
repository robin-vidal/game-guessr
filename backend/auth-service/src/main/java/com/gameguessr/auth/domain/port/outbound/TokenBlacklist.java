package com.gameguessr.auth.domain.port.outbound;

public interface TokenBlacklist {

    void invalidate(String token);

    boolean isInvalidated(String token);

    void cleanExpired();
}