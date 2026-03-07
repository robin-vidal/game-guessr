package com.gameguessr.lobby.application.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for room details.
 */
@Data
@Builder
public class RoomResponse {

    private String roomCode;
    private String hostId;
    private boolean isPrivate;
    private String status;
    private int maxPlayers;
    private SettingsResponse settings;
    private List<PlayerResponse> players;
    private Instant createdAt;

    @Data
    @Builder
    public static class SettingsResponse {
        private int roundCount;
        private int timeLimitSeconds;
        private String gamePack;
    }

    @Data
    @Builder
    public static class PlayerResponse {
        private String playerId;
        private String displayName;
        private Instant joinedAt;
    }
}
