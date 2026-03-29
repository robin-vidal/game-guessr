package com.gameguessr.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.UUID;

@Getter
@Builder
@With
public class User {

    private final UUID id;

    private final String username;

    private final String password;
}