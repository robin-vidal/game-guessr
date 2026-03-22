package com.gameguessr.scoring.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a list of scores.
 */
@Data
@Builder
@Schema(description = "Scores for a room, optionally filtered by round")
public class ScoresResponse {

    @Schema(description = "Room code the scores belong to", example = "ABC123")
    private String roomCode;

    @Schema(description = "List of individual score records")
    private List<ScoreItem> scores;

    @Data
    @Builder
    @Schema(description = "A single score record for one player and one phase")
    public static class ScoreItem {

        @Schema(description = "Unique score record ID")
        private UUID id;

        @Schema(description = "Round number (1-based)", example = "1")
        private int roundNumber;

        @Schema(description = "Player's user ID", example = "user-uuid-456")
        private String playerId;

        @Schema(description = "Phase this score belongs to", example = "GAME", allowableValues = {"GAME", "LEVEL", "SPOT"})
        private String phase;

        @Schema(description = "Points earned for this phase", example = "1000")
        private int points;

        @Schema(description = "Whether the answer was correct", example = "true")
        private boolean correct;

        @Schema(description = "Time bonus applied in milliseconds (LEVEL phase only)", example = "350")
        private long timeBonusMs;

        @Schema(description = "Timestamp when the score was recorded")
        private Instant createdAt;
    }
}
