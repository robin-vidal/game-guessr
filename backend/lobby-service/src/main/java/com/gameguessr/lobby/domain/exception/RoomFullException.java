package com.gameguessr.lobby.domain.exception;

/**
 * Thrown when a room is full and cannot accept more players.
 */
public class RoomFullException extends RuntimeException {

    public RoomFullException(String roomCode) {
        super("Room is full: " + roomCode);
    }
}
