package com.gameguessr.auth.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InMemoryTokenBlacklist")
class InMemoryTokenBlacklistTest {

    private InMemoryTokenBlacklist blacklist;

    @BeforeEach
    void setUp() {
        blacklist = new InMemoryTokenBlacklist();
    }

    @Test
    @DisplayName("invalidate — adds token to blacklist")
    void invalidate_addsTokenToBlacklist() {
        String token = "some.jwt.token";

        blacklist.invalidate(token);

        assertThat(blacklist.isInvalidated(token)).isTrue();
    }

    @Test
    @DisplayName("isInvalidated — returns false for non-blacklisted token")
    void isInvalidated_nonBlacklistedToken_returnsFalse() {
        assertThat(blacklist.isInvalidated("nonexistent.token")).isFalse();
    }

    @Test
    @DisplayName("isInvalidated — returns true for blacklisted token")
    void isInvalidated_blacklistedToken_returnsTrue() {
        String token = "blacklisted.jwt.token";
        blacklist.invalidate(token);

        assertThat(blacklist.isInvalidated(token)).isTrue();
    }

    @Test
    @DisplayName("cleanExpired — removes expired tokens")
    void cleanExpired_removesExpiredTokens() {
        blacklist.invalidate("token1");
        blacklist.invalidate("token2");

        int sizeBefore = blacklist.size();
        blacklist.cleanExpired();

        assertThat(blacklist.size()).isEqualTo(sizeBefore);
    }

    @Test
    @DisplayName("multiple invalidations — all tokens are blacklisted")
    void multipleInvalidations_allTokensBlacklisted() {
        String token1 = "token1";
        String token2 = "token2";
        String token3 = "token3";

        blacklist.invalidate(token1);
        blacklist.invalidate(token2);
        blacklist.invalidate(token3);

        assertThat(blacklist.isInvalidated(token1)).isTrue();
        assertThat(blacklist.isInvalidated(token2)).isTrue();
        assertThat(blacklist.isInvalidated(token3)).isTrue();
    }
}