package com.gameguessr.lobby.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Room aggregate root.
 * Pure domain object — zero framework dependencies.
 */
@Getter
@Builder
@With
public class Room {

    private final String roomCode;
    private final String hostId;
    private final boolean isPrivate;

    private RoomStatus status;
    private RoomSettings settings;

    @Builder.Default
    private final int maxPlayers = 8;

    @Builder.Default
    private final List<RoomPlayer> players = new ArrayList<>();

    private final Instant createdAt;

    // ── Business behaviour ──────────────────────────────────────────

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public boolean hasPlayer(String playerId) {
        return players.stream().anyMatch(p -> p.getPlayerId().equals(playerId));
    }

    public boolean isHost(String playerId) {
        return hostId.equals(playerId);
    }

    public boolean canJoin() {
        return status == RoomStatus.OPEN && !isFull();
    }
}
