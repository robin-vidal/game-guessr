package com.gameguessr.lobby.domain.port.outbound;

import com.gameguessr.lobby.domain.model.Room;

/**
 * Outbound port — Kafka event publishing contract for lobby events.
 * Implemented by the Kafka adapter in the infrastructure layer.
 */
public interface LobbyEventPublisher {

    /**
     * Publishes a {@code game.room.events} event when a room is created.
     * Consumed by the Game Service to pre-create a WAITING match.
     *
     * @param room the newly created room
     */
    void publishRoomCreated(Room room);
}
