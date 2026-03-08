package com.gameguessr.scoring.domain.port.inbound;

import com.gameguessr.scoring.domain.model.Score;

import java.util.List;

/**
 * Inbound port — defines all use cases for the Scoring Service.
 */
public interface ScoringUseCase {

    /**
     * Scores a guess event received from Kafka.
     *
     * @param roomCode    the room identifier
     * @param roundNumber the round number
     * @param playerId    the player's ID
     * @param phase       the guess phase (GAME, LEVEL, SPOT)
     * @param textAnswer  the text answer (for GAME and LEVEL phases)
     * @param guessX      guessed X coordinate (SPOT phase)
     * @param guessY      guessed Y coordinate (SPOT phase)
     * @param guessZ      guessed Z coordinate (SPOT phase)
     * @param submittedAt timestamp when the guess was submitted
     * @return the computed {@link Score}
     */
    Score scoreGuess(String roomCode, int roundNumber, String playerId,
            String phase, String textAnswer,
            Double guessX, Double guessY, Double guessZ,
            String submittedAt);

    /**
     * Returns all scores for a specific round.
     */
    List<Score> getRoundScores(String roomCode, int roundNumber);

    /**
     * Returns all scores for a match.
     */
    List<Score> getMatchScores(String roomCode);
}
