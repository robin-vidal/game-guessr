package com.gameguessr.game.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.UUID;

/**
 * A single round within a Match.
 * Belongs to the Match aggregate.
 */
@Getter
@Builder
@With
public class Round {

    private final UUID id;

    /** 1-based round number (1..5). */
    private final int roundNumber;

    /** The game/level entry for this round. */
    private final GamePackEntry gamePackEntry;

    /** Which phase the round is currently in. */
    private final GuessPhase currentPhase;

    /** Whether this round has been completed. */
    private final boolean finished;

    /** Timestamp (epoch ms) when this round started — used to compute time bonuses. */
    private final long startedAt;
}
