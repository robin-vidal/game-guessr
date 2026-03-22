package com.gameguessr.game.domain.exception;

/**
 * Thrown when a player submits a guess for a phase they have already guessed in.
 */
public class DuplicateGuessException extends RuntimeException {

    public DuplicateGuessException(String message) {
        super(message);
    }
}
