package com.gameguessr.scoring.infrastructure.messaging.producer;

import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.domain.port.outbound.ScoringEventPublisher;
import com.gameguessr.scoring.infrastructure.messaging.event.ScoreCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaScoringEventPublisher implements ScoringEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.score-calculated}")
    private String scoreCalculatedTopic;

    @Override
    public void publishScoreCalculated(Score score) {
        ScoreCalculatedEvent event = ScoreCalculatedEvent.builder()
                .roomCode(score.getRoomCode())
                .roundNumber(score.getRoundNumber())
                .playerId(score.getPlayerId())
                .phase(score.getPhase())
                .points(score.getPoints())
                .correct(score.isCorrect())
                .createdAt(score.getCreatedAt())
                .build();

        kafkaTemplate.send(scoreCalculatedTopic, score.getRoomCode(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish score.calculated for room {}: {}",
                                score.getRoomCode(), ex.getMessage());
                    } else {
                        log.debug("Published score.calculated to {} [room={}, player={}]",
                                scoreCalculatedTopic, score.getRoomCode(), score.getPlayerId());
                    }
                });
    }
}
