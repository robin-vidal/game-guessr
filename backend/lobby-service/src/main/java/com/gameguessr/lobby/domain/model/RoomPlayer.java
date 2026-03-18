package com.gameguessr.lobby.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Value object representing a player in a room.
 */
@Getter
@Builder
public class RoomPlayer {

    private final String playerId;
    private final String displayName;
    private final Instant joinedAt;
}
