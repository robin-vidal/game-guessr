package com.gameguessr.scoring.infrastructure.messaging.producer;

import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.infrastructure.messaging.event.ScoreCalculatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaScoringEventPublisher")
class KafkaScoringEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaScoringEventPublisher publisher;

    private static final String ROOM_CODE = "ABC123";
    private static final String PLAYER_ID = "player-1";
    private static final String SCORE_TOPIC = "score.calculated";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ReflectionTestUtils.setField(publisher, "scoreCalculatedTopic", SCORE_TOPIC);
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("publishScoreCalculated — sends event with correct fields to score topic")
    void publishScoreCalculated_sendsCorrectEvent() {
        Score score = Score.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .roundNumber(1)
                .playerId(PLAYER_ID)
                .phase("GAME")
                .points(1000)
                .correct(true)
                .timeBonusMs(0)
                .distanceError(0.0)
                .createdAt(Instant.now())
                .build();

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        publisher.publishScoreCalculated(score);

        verify(kafkaTemplate).send(eq(SCORE_TOPIC), eq(ROOM_CODE), eventCaptor.capture());
        ScoreCalculatedEvent event = (ScoreCalculatedEvent) eventCaptor.getValue();
        assertThat(event.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(event.getPlayerId()).isEqualTo(PLAYER_ID);
        assertThat(event.getPoints()).isEqualTo(1000);
        assertThat(event.isCorrect()).isTrue();
        assertThat(event.getPhase()).isEqualTo("GAME");
    }
}
