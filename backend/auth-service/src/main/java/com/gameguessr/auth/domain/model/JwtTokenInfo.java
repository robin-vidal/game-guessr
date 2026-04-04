package com.gameguessr.auth.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JwtTokenInfo {

    private final UUID userId;
    private final String username;
}