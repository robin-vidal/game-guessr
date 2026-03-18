package com.gameguessr.leaderboard.application.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for leaderboard rankings.
 */
@Data
@Builder
public class LeaderboardResponse {

    private String leaderboardType;
    private String identifier;
    private List<EntryResponse> entries;

    @Data
    @Builder
    public static class EntryResponse {
        private long rank;
        private String playerId;
        private double score;
    }
}
