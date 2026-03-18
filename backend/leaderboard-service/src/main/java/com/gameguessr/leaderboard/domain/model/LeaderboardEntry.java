package com.gameguessr.leaderboard.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents a single entry in the leaderboard rankings.
 */
@Getter
@Builder
public class LeaderboardEntry {

    private final String playerId;
    private final double score;
    private final long rank;
}
