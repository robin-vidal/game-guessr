package com.gameguessr.game.domain.port.inbound;

import com.gameguessr.game.domain.model.Guess;
import com.gameguessr.game.domain.model.Match;
import com.gameguessr.game.domain.model.Round;

import java.util.List;

/**
 * Inbound port — defines all use cases for the Game Service.
 * Implemented by the application layer. Consumed by the REST adapter.
 */
public interface GameUseCase {

    /**
     * Transitions a room's match to IN_PROGRESS.
     * Generates 5 {@link Round}s with placeholder (0,0,0) coordinates.
     *
     * @param roomCode the room identifier from the Lobby Service
     * @param hostId   the userId of the player starting the match
     * @return the newly started {@link Match}
     */
    Match startMatch(String roomCode, String hostId);

    /**
     * Returns the current round for a given room.
     * The response purposely omits the true spawn coordinates.
     *
     * @param roomCode the room identifier
     * @return the current {@link Round}
     */
    Round getCurrentRound(String roomCode);

    /**
     * Accepts a player guess, validates the phase, and publishes a
     * {@code player.guess.submitted} event to Kafka.
     *
     * @param roomCode the room identifier
     * @param guess    the player's guess including phase and answer
     */
    void submitGuess(String roomCode, Guess guess);

    /**
     * Returns final round results once the match is FINISHED.
     *
     * @param roomCode the room identifier
     * @return ordered list of rounds with their game pack entries
     */
    List<Round> getResults(String roomCode);
}
