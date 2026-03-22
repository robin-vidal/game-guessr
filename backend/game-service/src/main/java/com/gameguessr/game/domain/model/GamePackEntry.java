package com.gameguessr.game.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Value object representing a single game pack entry for a round.
 */
@Getter
@Builder
public class GamePackEntry {

    private final String gameId;
    private final String levelId;
    private final String noclipHash;

    @Builder.Default
    private final double spawnX = 0.0;

    @Builder.Default
    private final double spawnZ = 0.0;
}
