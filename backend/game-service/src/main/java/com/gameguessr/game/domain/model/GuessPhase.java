package com.gameguessr.game.domain.model;

/**
 * The three guessing phases within each round.
 */
public enum GuessPhase {
    /** Phase 1 — guess which game (e.g. "Mario Kart 8"). */
    GAME,
    /** Phase 2 — guess which level / track. Unlocked after GAME correct. */
    LEVEL,
    /** Phase 3 — pin the exact spawn location on top-down map. Unlocked after LEVEL correct. */
    SPOT
}
