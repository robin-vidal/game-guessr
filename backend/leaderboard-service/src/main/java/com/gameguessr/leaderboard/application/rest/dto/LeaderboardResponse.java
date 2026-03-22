package com.gameguessr.leaderboard.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for leaderboard rankings.
 */
@Data
@Builder
@Schema(description = "Ranked leaderboard entries for a room or globally")
public class LeaderboardResponse {

    @Schema(description = "Type of leaderboard", example = "room", allowableValues = {"room", "global"})
    private String leaderboardType;

    @Schema(description = "Room code for room leaderboard, or 'all' for global", example = "ABC123")
    private String identifier;

    @Schema(description = "Ranked list of players and their scores")
    private List<EntryResponse> entries;

    @Data
    @Builder
    @Schema(description = "A single leaderboard entry")
    public static class EntryResponse {

        @Schema(description = "Player's rank (1 = highest)", example = "1")
        private long rank;

        @Schema(description = "Player's user ID", example = "user-uuid-456")
        private String playerId;

        @Schema(description = "Player's total score", example = "2500.0")
        private double score;
    }
}
