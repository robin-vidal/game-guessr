package com.gameguessr.scoring.infrastructure.messaging.consumer;

import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.domain.port.inbound.ScoringUseCase;
import com.gameguessr.scoring.infrastructure.messaging.event.GuessSubmittedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuessEventConsumer")
class GuessEventConsumerTest {

    @Mock
    private ScoringUseCase scoringUseCase;

    @InjectMocks
    private GuessEventConsumer consumer;

    private static final String ROOM_CODE = "ABC123";
    private static final String PLAYER_ID = "player-1";
    private static final String CORRECT_GAME_ID = "mario-kart-wii";
    private static final String CORRECT_LEVEL_ID = "Luigi Circuit";

    @Test
    @DisplayName("onGuessSubmitted — delegates GAME guess with correct answers to scoringUseCase")
    void onGuessSubmitted_gamePhase_delegatesToService() {
        GuessSubmittedEvent event = buildEvent("GAME", "Mario Kart 8",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID, null, null, null, null);
        Score mockScore = buildMockScore();
        when(scoringUseCase.scoreGuess(anyString(), anyInt(), anyString(), anyString(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(mockScore);

        consumer.onGuessSubmitted(event);

        verify(scoringUseCase).scoreGuess(
                eq(ROOM_CODE),
                eq(1),
                eq(PLAYER_ID),
                eq("GAME"),
                eq("Mario Kart 8"),
                eq(CORRECT_GAME_ID),
                eq(CORRECT_LEVEL_ID),
                isNull(), isNull(), isNull(),
                isNull());
    }

    @Test
    @DisplayName("onGuessSubmitted — delegates LEVEL guess with submittedAt")
    void onGuessSubmitted_levelPhase_delegatesWithSubmittedAt() {
        Instant submittedAt = Instant.now();
        GuessSubmittedEvent event = buildEvent("LEVEL", "Luigi Circuit",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID, null, null, null, submittedAt);
        Score mockScore = buildMockScore();
        when(scoringUseCase.scoreGuess(anyString(), anyInt(), anyString(), anyString(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(mockScore);

        consumer.onGuessSubmitted(event);

        verify(scoringUseCase).scoreGuess(
                eq(ROOM_CODE), eq(1), eq(PLAYER_ID), eq("LEVEL"),
                eq("Luigi Circuit"),
                eq(CORRECT_GAME_ID), eq(CORRECT_LEVEL_ID),
                isNull(), isNull(), isNull(),
                eq(submittedAt.toString()));
    }

    @Test
    @DisplayName("onGuessSubmitted — passes null for submittedAt when event has no timestamp")
    void onGuessSubmitted_nullSubmittedAt_passesNull() {
        GuessSubmittedEvent event = buildEvent("GAME", "Mario Kart 8",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID, null, null, null, null);
        when(scoringUseCase.scoreGuess(anyString(), anyInt(), anyString(), anyString(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(buildMockScore());

        consumer.onGuessSubmitted(event);

        verify(scoringUseCase).scoreGuess(
                any(), anyInt(), any(), any(), any(),
                any(), any(), any(), any(), any(), isNull());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private GuessSubmittedEvent buildEvent(String phase, String textAnswer,
            String correctGameId, String correctLevelId,
            Double guessX, Double guessY, Double guessZ, Instant submittedAt) {
        GuessSubmittedEvent event = new GuessSubmittedEvent();
        event.setRoomCode(ROOM_CODE);
        event.setRoundNumber(1);
        event.setPlayerId(PLAYER_ID);
        event.setPhase(phase);
        event.setTextAnswer(textAnswer);
        event.setCorrectGameId(correctGameId);
        event.setCorrectLevelId(correctLevelId);
        event.setGuessX(guessX);
        event.setGuessY(guessY);
        event.setGuessZ(guessZ);
        event.setSubmittedAt(submittedAt);
        return event;
    }

    private Score buildMockScore() {
        return Score.builder()
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
    }
}
