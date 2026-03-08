package com.gameguessr.lobby.domain.exception;

/**
 * Thrown when a room cannot be found by its code.
 */
public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(String roomCode) {
        super("Room not found: " + roomCode);
    }
}
