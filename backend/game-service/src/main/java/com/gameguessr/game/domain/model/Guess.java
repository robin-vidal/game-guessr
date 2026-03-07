package com.gameguessr.game.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Value object representing a player's guess for a specific phase.
 */
@Getter
@Builder
public class Guess {

    private final String playerId;

    private final GuessPhase phase;

    /**
     * The textual answer for GAME or LEVEL phases (e.g. "Mario Kart 8",
     * "BabyPark").
     * Null for SPOT phase.
     */
    private final String textAnswer;

    /** Guessed X coordinate — only populated for SPOT phase. */
    private final Double guessX;

    /** Guessed Y coordinate — only populated for SPOT phase. */
    private final Double guessY;

    /** Guessed Z coordinate — only populated for SPOT phase. */
    private final Double guessZ;

    private final Instant submittedAt;
}
