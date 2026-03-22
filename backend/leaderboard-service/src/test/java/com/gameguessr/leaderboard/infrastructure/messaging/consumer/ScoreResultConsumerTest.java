package com.gameguessr.leaderboard.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gameguessr.leaderboard.domain.port.inbound.LeaderboardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoreResultConsumer")
class ScoreResultConsumerTest {

    @Mock
    private LeaderboardUseCase leaderboardUseCase;

    @InjectMocks
    private ScoreResultConsumer consumer;

    @BeforeEach
    void setUp() {
        // Inject a real ObjectMapper with JavaTimeModule for Instant deserialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        org.springframework.test.util.ReflectionTestUtils.setField(consumer, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("onScoreCalculated — parses valid JSON and delegates to leaderboardUseCase")
    void onScoreCalculated_validJson_delegatesToUseCase() {
        String payload = """
                {
                    "roomCode": "ABC123",
                    "roundNumber": 1,
                    "playerId": "player-1",
                    "phase": "GAME",
                    "points": 1000,
                    "correct": true,
                    "createdAt": "2024-01-01T10:00:00Z"
                }
                """;

        consumer.onScoreCalculated(payload);

        verify(leaderboardUseCase).recordScore("ABC123", "player-1", 1000);
    }

    @Test
    @DisplayName("onScoreCalculated — invalid JSON is handled gracefully without throwing")
    void onScoreCalculated_invalidJson_handledGracefully() {
        String invalidPayload = "not-valid-json";

        // Should not throw — consumer logs and swallows the error
        consumer.onScoreCalculated(invalidPayload);

        verify(leaderboardUseCase, never()).recordScore(any(), any(), anyInt());
    }
}
