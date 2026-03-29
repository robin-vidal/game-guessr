package com.gameguessr.game.infrastructure.messaging.producer;

import com.gameguessr.game.domain.model.GamePackEntry;
import com.gameguessr.game.domain.model.Guess;
import com.gameguessr.game.domain.model.GuessPhase;
import com.gameguessr.game.domain.model.Round;
import com.gameguessr.game.infrastructure.messaging.event.GuessSubmittedEvent;
import com.gameguessr.game.infrastructure.messaging.event.RoundUpdateEvent;
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
@DisplayName("KafkaGameEventPublisher")
class KafkaGameEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaGameEventPublisher publisher;

    private static final String ROOM_CODE = "ABC123";
    private static final String GUESS_TOPIC = "player.guess.submitted";
    private static final String ROUND_TOPIC = "game.round.update";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ReflectionTestUtils.setField(publisher, "guessSubmittedTopic", GUESS_TOPIC);
        ReflectionTestUtils.setField(publisher, "roundUpdateTopic", ROUND_TOPIC);
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("publishGuessSubmitted — sends event with correct fields to guess topic")
    void publishGuessSubmitted_sendsCorrectEvent() {
        Guess guess = Guess.builder()
                .playerId("player-1")
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart Wii")
                .submittedAt(Instant.now())
                .build();

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        publisher.publishGuessSubmitted(ROOM_CODE, 1, guess, "mario-kart-wii", "Luigi Circuit");

        verify(kafkaTemplate).send(eq(GUESS_TOPIC), eq(ROOM_CODE), eventCaptor.capture());
        GuessSubmittedEvent event = (GuessSubmittedEvent) eventCaptor.getValue();
        assertThat(event.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(event.getRoundNumber()).isEqualTo(1);
        assertThat(event.getPlayerId()).isEqualTo("player-1");
        assertThat(event.getPhase()).isEqualTo("GAME");
        assertThat(event.getTextAnswer()).isEqualTo("Mario Kart Wii");
        assertThat(event.getCorrectGameId()).isEqualTo("mario-kart-wii");
        assertThat(event.getCorrectLevelId()).isEqualTo("Luigi Circuit");
    }

    @Test
    @DisplayName("publishRoundUpdate — sends event with noclipHash to round topic")
    void publishRoundUpdate_sendsCorrectEvent() {
        Round round = Round.builder()
                .id(UUID.randomUUID())
                .roundNumber(1)
                .gamePackEntry(GamePackEntry.builder()
                        .gameId("mario-kart-wii")
                        .levelId("Luigi Circuit")
                        .noclipHash("mkwii/beginner_course;ShareData=test")
                        .build())
                .currentPhase(GuessPhase.GAME)
                .finished(false)
                .startedAt(Instant.now().toEpochMilli())
                .build();

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        publisher.publishRoundUpdate(ROOM_CODE, round);

        verify(kafkaTemplate).send(eq(ROUND_TOPIC), eq(ROOM_CODE), eventCaptor.capture());
        RoundUpdateEvent event = (RoundUpdateEvent) eventCaptor.getValue();
        assertThat(event.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(event.getRoundNumber()).isEqualTo(1);
        assertThat(event.getCurrentPhase()).isEqualTo("GAME");
        assertThat(event.isFinished()).isFalse();
        assertThat(event.getGameId()).isEqualTo("mario-kart-wii");
        assertThat(event.getNoclipHash()).isEqualTo("mkwii/beginner_course;ShareData=test");
    }
}
