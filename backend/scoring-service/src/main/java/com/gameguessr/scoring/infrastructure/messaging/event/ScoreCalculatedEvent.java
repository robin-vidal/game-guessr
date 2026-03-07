package com.gameguessr.scoring.infrastructure.messaging.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Kafka event payload for {@code score.calculated}.
 * Consumed by the Leaderboard Service.
 */
@Data
@Builder
public class ScoreCalculatedEvent {

    private String roomCode;
    private int roundNumber;
    private String playerId;
    private String phase;
    private int points;
    private boolean correct;
    private Instant createdAt;
}
