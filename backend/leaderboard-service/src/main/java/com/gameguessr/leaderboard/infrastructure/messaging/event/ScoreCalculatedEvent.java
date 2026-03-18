package com.gameguessr.leaderboard.infrastructure.messaging.event;

import lombok.Data;

import java.time.Instant;

/**
 * Kafka event payload consumed from {@code score.calculated}.
 * Must match the schema produced by scoring-service's ScoreCalculatedEvent.
 */
@Data
public class ScoreCalculatedEvent {

    private String roomCode;
    private int roundNumber;
    private String playerId;
    private String phase;
    private int points;
    private boolean correct;
    private Instant createdAt;
}
