package com.gameguessr.leaderboard.infrastructure.messaging.consumer;

import com.gameguessr.leaderboard.domain.port.inbound.LeaderboardUseCase;
import com.gameguessr.leaderboard.infrastructure.messaging.event.ScoreCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code score.calculated} events from Scoring Service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreResultConsumer {

    private final LeaderboardUseCase leaderboardUseCase;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.score-calculated}", groupId = "${spring.kafka.consumer.group-id}")
    public void onScoreCalculated(String payload) {
        log.info("RAW KAFKA PAYLOAD RECEIVED IN LEADERBOARD: {}", payload);
        try {
            ScoreCalculatedEvent event = objectMapper.readValue(payload, ScoreCalculatedEvent.class);
            log.info("Successfully deserialized into ScoreCalculatedEvent for room={}, round={}, player={}",
                    event.getRoomCode(), event.getRoundNumber(), event.getPlayerId());

            leaderboardUseCase.recordScore(
                    event.getRoomCode(),
                    event.getPlayerId(),
                    event.getPoints());
        } catch (Exception e) {
            log.error("Failed to parse score.calculated event payload: {}", payload, e);
        }
    }
}
