package com.gameguessr.game.domain.port.outbound;

import com.gameguessr.game.domain.model.Guess;
import com.gameguessr.game.domain.model.Round;

/**
 * Outbound port — Kafka event publishing contract.
 * Implemented by the Kafka adapter in the infrastructure layer.
 */
public interface GameEventPublisher {

    /**
     * Publishes a {@code player.guess.submitted} event.
     * Consumed by the Scoring Service.
     *
     * @param roomCode the room identifier
     * @param roundNumber the current round number
     * @param guess    the submitted guess
     */
    void publishGuessSubmitted(String roomCode, int roundNumber, Guess guess);

    /**
     * Publishes a {@code game.round.update} event.
     * Consumed by the WebSocket Gateway for real-time sync.
     *
     * @param roomCode    the room identifier
     * @param round       the round that was updated
     */
    void publishRoundUpdate(String roomCode, Round round);
}
