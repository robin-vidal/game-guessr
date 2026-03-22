package com.gameguessr.scoring.application.rest;

import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.domain.port.inbound.ScoringUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScoringController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ScoringController")
class ScoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScoringUseCase scoringUseCase;

    private static final String ROOM_CODE = "ABC123";
    private static final String PLAYER_ID = "player-1";

    // ── GET /scoring/{roomCode}/rounds/{roundNumber} ──────────────────

    @Test
    @DisplayName("GET /scoring/{roomCode}/rounds/{roundNumber} — 200 with round scores")
    void getRoundScores_returns200() throws Exception {
        List<Score> scores = List.of(buildScore(1, "GAME", 1000, true));
        when(scoringUseCase.getRoundScores(ROOM_CODE, 1)).thenReturn(scores);

        mockMvc.perform(get("/api/v1/scoring/{roomCode}/rounds/{roundNumber}", ROOM_CODE, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCode").value(ROOM_CODE))
                .andExpect(jsonPath("$.scores").isArray())
                .andExpect(jsonPath("$.scores[0].playerId").value(PLAYER_ID))
                .andExpect(jsonPath("$.scores[0].points").value(1000));
    }

    @Test
    @DisplayName("GET /scoring/{roomCode}/rounds/{roundNumber} — 200 with empty list")
    void getRoundScores_empty_returns200() throws Exception {
        when(scoringUseCase.getRoundScores(ROOM_CODE, 1)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/scoring/{roomCode}/rounds/{roundNumber}", ROOM_CODE, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores").isArray())
                .andExpect(jsonPath("$.scores").isEmpty());
    }

    // ── GET /scoring/{roomCode} ───────────────────────────────────────

    @Test
    @DisplayName("GET /scoring/{roomCode} — 200 with all match scores")
    void getMatchScores_returns200() throws Exception {
        List<Score> scores = List.of(
                buildScore(1, "GAME", 1000, true),
                buildScore(1, "LEVEL", 650, true));
        when(scoringUseCase.getMatchScores(ROOM_CODE)).thenReturn(scores);

        mockMvc.perform(get("/api/v1/scoring/{roomCode}", ROOM_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCode").value(ROOM_CODE))
                .andExpect(jsonPath("$.scores").isArray())
                .andExpect(jsonPath("$.scores.length()").value(2));
    }

    @Test
    @DisplayName("GET /scoring/{roomCode} — response includes correct and timeBonusMs fields")
    void getMatchScores_responseMapping() throws Exception {
        Score score = buildScore(1, "LEVEL", 650, true);
        when(scoringUseCase.getMatchScores(ROOM_CODE)).thenReturn(List.of(score));

        mockMvc.perform(get("/api/v1/scoring/{roomCode}", ROOM_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores[0].correct").value(true))
                .andExpect(jsonPath("$.scores[0].phase").value("LEVEL"));
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Score buildScore(int roundNumber, String phase, int points, boolean correct) {
        return Score.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .roundNumber(roundNumber)
                .playerId(PLAYER_ID)
                .phase(phase)
                .points(points)
                .correct(correct)
                .timeBonusMs(0)
                .distanceError(0.0)
                .createdAt(Instant.now())
                .build();
    }
}
