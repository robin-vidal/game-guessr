package com.gameguessr.game.domain.exception;

/**
 * Thrown when a player attempts to submit a guess for a phase that is not yet unlocked.
 */
public class InvalidPhaseException extends RuntimeException {

    public InvalidPhaseException(String message) {
        super(message);
    }
}
