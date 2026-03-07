package com.gameguessr.lobby.domain.exception;

/**
 * Thrown when a non-host player attempts a host-only action.
 */
public class NotHostException extends RuntimeException {

    public NotHostException(String playerId, String roomCode) {
        super("Player " + playerId + " is not the host of room " + roomCode);
    }
}
