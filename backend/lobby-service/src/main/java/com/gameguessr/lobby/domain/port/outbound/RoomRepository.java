package com.gameguessr.lobby.domain.port.outbound;

import com.gameguessr.lobby.domain.model.Room;
import com.gameguessr.lobby.domain.model.RoomStatus;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port — persistence contract for Room aggregates.
 * Implemented by the Redis adapter in the infrastructure layer.
 */
public interface RoomRepository {

    /** Persist or update a room. */
    Room save(Room room);

    /** Find a room by its code. */
    Optional<Room> findByCode(String roomCode);

    /** Find all rooms with the given status. */
    List<Room> findByStatus(RoomStatus status);

    /** Check whether a room exists for a given code. */
    boolean existsByCode(String roomCode);

    /** Delete a room by code. */
    void deleteByCode(String roomCode);
}
