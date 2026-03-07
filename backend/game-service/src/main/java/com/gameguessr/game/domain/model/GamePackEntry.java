package com.gameguessr.game.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Value object representing a single game pack entry.
 *
 * <p>
 * Coordinates default to (0, 0, 0) for MVP.
 * Replace with actual spawn coordinates when the game pack data source is wired
 * in.
 * </p>
 */
@Getter
@Builder
public class GamePackEntry {

    /** Human-readable game name (e.g. "Mario Kart 8"). */
    private final String gameId;

    /** Level / track identifier (e.g. "BabyPark"). */
    private final String levelId;

    /**
     * Spawn X coordinate.
     * TODO: replace with real coordinate from game pack data source.
     */
    @Builder.Default
    private final double spawnX = 0.0;

    /**
     * Spawn Y coordinate.
     * TODO: replace with real coordinate from game pack data source.
     */
    @Builder.Default
    private final double spawnY = 0.0;

    /**
     * Spawn Z coordinate.
     * TODO: replace with real coordinate from game pack data source.
     */
    @Builder.Default
    private final double spawnZ = 0.0;
}
