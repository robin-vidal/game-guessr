package com.gameguessr.lobby.domain.exception;

/**
 * Thrown when a player already exists in the room.
 */
public class PlayerAlreadyInRoomException extends RuntimeException {

    public PlayerAlreadyInRoomException(String playerId, String roomCode) {
        super("Player " + playerId + " is already in room " + roomCode);
    }
}
