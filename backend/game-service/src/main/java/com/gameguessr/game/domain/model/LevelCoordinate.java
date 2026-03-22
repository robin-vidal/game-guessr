package com.gameguessr.game.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LevelCoordinate {

    private final UUID id;
    private final String noclipHash;
    private final double spawnX;
    private final double spawnZ;
}
