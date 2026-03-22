package com.gameguessr.game.infrastructure.messaging.event;

import lombok.Data;

/**
 * Kafka event payload for {@code game.room.events} consumed from the Lobby
 * Service.
 * Signals that a room has been created and the game-service should prepare a
 * match.
 */
@Data
public class RoomCreatedEvent {

    private String roomCode;
    private String hostId;
    private boolean isPrivate;
    private String gamePack;
}
