package com.gameguessr.lobby.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Value object for room configuration settings.
 */
@Getter
@Builder
public class RoomSettings {

    /** Number of rounds per match (default: 5). */
    @Builder.Default
    private final int roundCount = 5;

    /** Time limit per round in seconds (default: 60). */
    @Builder.Default
    private final int timeLimitSeconds = 60;

    /** Selected game pack identifier (default: mario-kart). */
    @Builder.Default
    private final String gamePack = "mario-kart-wii";
}
