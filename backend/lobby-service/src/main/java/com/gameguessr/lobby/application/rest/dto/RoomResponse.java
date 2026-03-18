package com.gameguessr.lobby.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for room details.
 */
@Data
@Builder
@Schema(description = "Room details including players and settings")
public class RoomResponse {

    @Schema(description = "Unique 6-character room code", example = "ABC123")
    private String roomCode;

    @Schema(description = "User ID of the room host", example = "user-uuid-123")
    private String hostId;

    @Schema(description = "Whether the room is private (invite-only)", example = "false")
    private boolean isPrivate;

    @Schema(description = "Current room status", example = "OPEN", allowableValues = {"OPEN", "IN_PROGRESS", "FINISHED", "CLOSED"})
    private String status;

    @Schema(description = "Maximum number of players allowed", example = "8")
    private int maxPlayers;

    @Schema(description = "Room game settings")
    private SettingsResponse settings;

    @Schema(description = "List of players currently in the room")
    private List<PlayerResponse> players;

    @Schema(description = "Timestamp when the room was created")
    private Instant createdAt;

    @Data
    @Builder
    @Schema(description = "Game settings for the room")
    public static class SettingsResponse {

        @Schema(description = "Number of rounds per match", example = "5")
        private int roundCount;

        @Schema(description = "Time limit per round in seconds", example = "60")
        private int timeLimitSeconds;

        @Schema(description = "Game pack identifier", example = "mario-kart")
        private String gamePack;
    }

    @Data
    @Builder
    @Schema(description = "A player in the room")
    public static class PlayerResponse {

        @Schema(description = "Player's user ID", example = "user-uuid-456")
        private String playerId;

        @Schema(description = "Player's display name", example = "Mario")
        private String displayName;

        @Schema(description = "Timestamp when the player joined")
        private Instant joinedAt;
    }
}
