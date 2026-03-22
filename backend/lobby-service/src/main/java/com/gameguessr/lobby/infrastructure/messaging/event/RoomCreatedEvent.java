package com.gameguessr.lobby.infrastructure.messaging.event;

import lombok.Builder;
import lombok.Data;

/**
 * Kafka event payload for {@code game.room.events}.
 * Consumed by the Game Service to pre-create a WAITING match.
 * Must match the schema expected by game-service's RoomCreatedEvent consumer.
 */
@Data
@Builder
public class RoomCreatedEvent {

    private String roomCode;
    private String hostId;
    private boolean isPrivate;
    private String gamePack;
}
