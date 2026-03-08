package com.gameguessr.game.domain.exception;

/**
 * Thrown when trying to start a match that is not in WAITING status.
 */
public class MatchAlreadyStartedException extends RuntimeException {

    public MatchAlreadyStartedException(String roomCode) {
        super("Match for room " + roomCode + " has already been started.");
    }
}
