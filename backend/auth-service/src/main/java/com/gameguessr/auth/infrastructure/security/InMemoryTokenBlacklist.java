package com.gameguessr.auth.infrastructure.security;

import com.gameguessr.auth.domain.port.outbound.TokenBlacklist;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenBlacklist implements TokenBlacklist {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    @Override
    public void invalidate(String token) {
        long expirationTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        blacklistedTokens.put(token, expirationTime);
    }

    @Override
    public boolean isInvalidated(String token) {
        Long expiration = blacklistedTokens.get(token);
        if (expiration == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiration) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    @Override
    @Scheduled(fixedRate = 3600000)
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    int size() {
        return blacklistedTokens.size();
    }
}