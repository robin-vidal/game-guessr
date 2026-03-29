package com.gameguessr.scoring.infrastructure.messaging.consumer;

import com.gameguessr.scoring.domain.port.inbound.ScoringUseCase;
import com.gameguessr.scoring.infrastructure.messaging.event.GuessSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code player.guess.submitted} events from Game Service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuessEventConsumer {

    private final ScoringUseCase scoringUseCase;

    @KafkaListener(topics = "${kafka.topics.guess-submitted}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void onGuessSubmitted(GuessSubmittedEvent event) {
        log.info("Received guess event: player={}, room={}, round={}, phase={}",
                event.getPlayerId(), event.getRoomCode(), event.getRoundNumber(), event.getPhase());

        scoringUseCase.scoreGuess(
                event.getRoomCode(),
                event.getRoundNumber(),
                event.getPlayerId(),
                event.getPhase(),
                event.getTextAnswer(),
                event.getCorrectGameId(),
                event.getCorrectLevelId(),
                event.getGuessX(),
                event.getGuessY(),
                event.getGuessZ(),
                event.getSubmittedAt() != null ? event.getSubmittedAt().toString() : null);
    }
}
