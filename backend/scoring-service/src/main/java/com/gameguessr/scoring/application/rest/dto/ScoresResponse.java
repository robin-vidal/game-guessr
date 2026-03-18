package com.gameguessr.scoring.application.rest.dto;

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
public class ScoresResponse {

    private String roomCode;
    private List<ScoreItem> scores;

    @Data
    @Builder
    public static class ScoreItem {
        private UUID id;
        private int roundNumber;
        private String playerId;
        private String phase;
        private int points;
        private boolean correct;
        private long timeBonusMs;
        private Instant createdAt;
    }
}
