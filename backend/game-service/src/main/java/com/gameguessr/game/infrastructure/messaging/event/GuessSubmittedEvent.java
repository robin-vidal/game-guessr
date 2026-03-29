package com.gameguessr.game.infrastructure.messaging.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Kafka event payload for {@code player.guess.submitted}.
 * Consumed by the Scoring Service.
 */
@Data
@Builder
public class GuessSubmittedEvent {

    private String roomCode;
    private int roundNumber;
    private String playerId;

    /** GAME | LEVEL | SPOT */
    private String phase;

    /** Populated for GAME and LEVEL phases. */
    private String textAnswer;

    /** Correct answers — used by the Scoring Service for fuzzy matching. */
    private String correctGameId;   // slug, e.g. "mario-kart-wii"
    private String correctLevelId;  // level name, e.g. "Circuit Luigi"

    /** Populated for SPOT phase only. */
    private Double guessX;
    private Double guessY;
    private Double guessZ;

    private Instant submittedAt;
}
