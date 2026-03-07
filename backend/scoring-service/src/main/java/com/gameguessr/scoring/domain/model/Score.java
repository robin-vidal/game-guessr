package com.gameguessr.scoring.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Score aggregate — represents a scored guess for a single player/round/phase.
 */
@Getter
@Builder
public class Score {

    private final UUID id;
    private final String roomCode;
    private final int roundNumber;
    private final String playerId;

    /** GAME | LEVEL | SPOT */
    private final String phase;

    /** Points awarded for this phase. */
    private final int points;

    /** Whether the guess was correct (applicable to GAME and LEVEL). */
    private final boolean correct;

    /** Time bonus in milliseconds (applicable to LEVEL). */
    @Builder.Default
    private final long timeBonusMs = 0;

    /** Distance error (applicable to SPOT, post-MVP). */
    @Builder.Default
    private final double distanceError = 0.0;

    private final Instant createdAt;
}
