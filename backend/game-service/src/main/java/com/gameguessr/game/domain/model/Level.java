package com.gameguessr.game.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Level {

    private final UUID id;
    private final String gamePack;
    private final String levelName;
    private final List<LevelCoordinate> coordinates;
}
