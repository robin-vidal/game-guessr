package com.gameguessr.scoring.domain.exception;

/**
 * Thrown when scores cannot be found for a room/round.
 */
public class ScoreNotFoundException extends RuntimeException {

    public ScoreNotFoundException(String message) {
        super(message);
    }
}
