package com.gameguessr.game.domain.model;

/**
 * Lifecycle states for a Match.
 */
public enum MatchStatus {
    /** Room exists, waiting for host to start. */
    WAITING,
    /** Match is actively being played. */
    IN_PROGRESS,
    /** All rounds completed. */
    FINISHED
}
