package com.gameguessr.game.infrastructure.messaging.producer;

import com.gameguessr.game.domain.model.Guess;
import com.gameguessr.game.domain.model.Round;
import com.gameguessr.game.domain.port.outbound.GameEventPublisher;
import com.gameguessr.game.infrastructure.messaging.event.GuessSubmittedEvent;
import com.gameguessr.game.infrastructure.messaging.event.RoundUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Driven adapter — implements GameEventPublisher port using Apache Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaGameEventPublisher implements GameEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.guess-submitted}")
    private String guessSubmittedTopic;

    @Value("${kafka.topics.round-update}")
    private String roundUpdateTopic;

    @Override
    public void publishGuessSubmitted(String roomCode, int roundNumber, Guess guess) {
        GuessSubmittedEvent event = GuessSubmittedEvent.builder()
                .roomCode(roomCode)
                .roundNumber(roundNumber)
                .playerId(guess.getPlayerId())
                .phase(guess.getPhase().name())
                .textAnswer(guess.getTextAnswer())
                .guessX(guess.getGuessX())
                .guessY(guess.getGuessY())
                .guessZ(guess.getGuessZ())
                .submittedAt(guess.getSubmittedAt())
                .build();

        kafkaTemplate.send(guessSubmittedTopic, roomCode, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish guess event for room {}: {}", roomCode, ex.getMessage());
                    } else {
                        log.debug("Published guess event to topic {} [room={}]", guessSubmittedTopic, roomCode);
                    }
                });
    }

    @Override
    public void publishRoundUpdate(String roomCode, Round round) {
        RoundUpdateEvent event = RoundUpdateEvent.builder()
                .roomCode(roomCode)
                .roundNumber(round.getRoundNumber())
                .currentPhase(round.getCurrentPhase().name())
                .finished(round.isFinished())
                .startedAt(round.getStartedAt())
                .gameId(round.getGamePackEntry().getGameId())
                .levelId(round.getGamePackEntry().getLevelId())
                .build();

        kafkaTemplate.send(roundUpdateTopic, roomCode, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish round update for room {}: {}", roomCode, ex.getMessage());
                    } else {
                        log.debug("Published round update to topic {} [room={}, round={}]",
                                roundUpdateTopic, roomCode, round.getRoundNumber());
                    }
                });
    }
}
