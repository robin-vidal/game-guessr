package com.gameguessr.scoring.infrastructure.messaging.event;

import lombok.Data;

import java.time.Instant;

/**
 * Kafka event payload consumed from {@code player.guess.submitted}.
 * Must match the schema produced by game-service's GuessSubmittedEvent.
 */
@Data
public class GuessSubmittedEvent {

    private String roomCode;
    private int roundNumber;
    private String playerId;
    private String phase;
    private String textAnswer;
    private Double guessX;
    private Double guessY;
    private Double guessZ;
    private Instant submittedAt;
}
