package com.gameguessr.game.domain.exception;

/**
 * Thrown when no match is found for a given room code.
 */
public class MatchNotFoundException extends RuntimeException {

    public MatchNotFoundException(String roomCode) {
        super("Match not found for room: " + roomCode);
    }
}
