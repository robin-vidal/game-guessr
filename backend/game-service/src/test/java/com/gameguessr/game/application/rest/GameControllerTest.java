package com.gameguessr.game.application.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameguessr.game.application.rest.dto.GuessRequest;
import com.gameguessr.game.application.rest.dto.StartMatchRequest;
import com.gameguessr.game.domain.exception.MatchNotFoundException;
import com.gameguessr.game.domain.model.*;
import com.gameguessr.game.domain.port.inbound.GameUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("GameController")
class GameControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private GameUseCase gameUseCase;

        private static final String ROOM_CODE = "ABC123";

        // ── POST /start ───────────────────────────────────────────────────

        @Test
        @DisplayName("POST /start — 201 when match started successfully")
        void startMatch_returns201() throws Exception {
                Match match = buildInProgressMatch();
                when(gameUseCase.startMatch(eq(ROOM_CODE), any(), any())).thenReturn(match);

                StartMatchRequest req = new StartMatchRequest();
                req.setHostId("host-1");
                req.setPlayerIds(List.of("host-1", "player-2"));

                mockMvc.perform(post("/api/v1/games/{code}/start", ROOM_CODE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /start — 400 when hostId is blank")
        void startMatch_blankHostId_returns400() throws Exception {
                StartMatchRequest req = new StartMatchRequest();
                req.setHostId("");
                req.setPlayerIds(List.of("player-1"));

                mockMvc.perform(post("/api/v1/games/{code}/start", ROOM_CODE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest());
        }

        // ── GET /round ────────────────────────────────────────────────────

        @Test
        @DisplayName("GET /round — 200 with round info including noclipHash")
        void getCurrentRound_returns200() throws Exception {
                Round round = buildRound(1, GuessPhase.GAME);
                when(gameUseCase.getCurrentRound(ROOM_CODE)).thenReturn(round);

                mockMvc.perform(get("/api/v1/games/{code}/round", ROOM_CODE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.roundNumber").value(1))
                                .andExpect(jsonPath("$.currentPhase").value("GAME"))
                                .andExpect(jsonPath("$.gameId").value("mario-kart-wii"))
                                .andExpect(jsonPath("$.noclipHash").value("mkwii/beginner_course;ShareData=test"));
        }

        @Test
        @DisplayName("GET /round — 404 when room not found")
        void getCurrentRound_notFound_returns404() throws Exception {
                when(gameUseCase.getCurrentRound(ROOM_CODE))
                        .thenThrow(new MatchNotFoundException(ROOM_CODE));

                mockMvc.perform(get("/api/v1/games/{code}/round", ROOM_CODE))
                        .andExpect(status().isNotFound());
        }

        // ── POST /guess ───────────────────────────────────────────────────

        @Test
        @DisplayName("POST /guess — 202 for valid GAME phase guess")
        void submitGuess_gamePhase_returns202() throws Exception {
                doNothing().when(gameUseCase).submitGuess(eq(ROOM_CODE), any());

                GuessRequest req = new GuessRequest();
                req.setPlayerId("player-1");
                req.setPhase("GAME");
                req.setTextAnswer("Mario Kart Wii");

                mockMvc.perform(post("/api/v1/games/{code}/guess", ROOM_CODE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("POST /guess — 400 for invalid phase value")
        void submitGuess_invalidPhase_returns400() throws Exception {
                GuessRequest req = new GuessRequest();
                req.setPlayerId("player-1");
                req.setPhase("INVALID");

                mockMvc.perform(post("/api/v1/games/{code}/guess", ROOM_CODE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest());
        }

        // ── GET /results ──────────────────────────────────────────────────

        @Test
        @DisplayName("GET /results — 200 with rounds including noclipHash and no trueSpawnY")
        void getResults_returns200() throws Exception {
                List<Round> rounds = List.of(buildRound(1, GuessPhase.SPOT));
                when(gameUseCase.getResults(ROOM_CODE)).thenReturn(rounds);

                mockMvc.perform(get("/api/v1/games/{code}/results", ROOM_CODE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.roomCode").value(ROOM_CODE))
                                .andExpect(jsonPath("$.rounds").isArray())
                                .andExpect(jsonPath("$.rounds[0].noclipHash").value("mkwii/beginner_course;ShareData=test"))
                                .andExpect(jsonPath("$.rounds[0].trueSpawnX").value(0.0))
                                .andExpect(jsonPath("$.rounds[0].trueSpawnZ").value(0.0))
                                .andExpect(jsonPath("$.rounds[0].trueSpawnY").doesNotExist());
        }

        // ── Additional coverage ───────────────────────────────────────────

        @Test
        @DisplayName("POST /start — 409 when match already started")
        void startMatch_alreadyStarted_returns409() throws Exception {
                when(gameUseCase.startMatch(eq(ROOM_CODE), any(), any()))
                                .thenThrow(new com.gameguessr.game.domain.exception.MatchAlreadyStartedException(ROOM_CODE));

                StartMatchRequest req = new StartMatchRequest();
                req.setHostId("host-1");
                req.setPlayerIds(List.of("host-1"));

                mockMvc.perform(post("/api/v1/games/{code}/start", ROOM_CODE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("GET /round — 409 when match is not in progress")
        void getCurrentRound_notInProgress_returns409() throws Exception {
                when(gameUseCase.getCurrentRound(ROOM_CODE))
                                .thenThrow(new IllegalStateException("Match is not in progress"));

                mockMvc.perform(get("/api/v1/games/{code}/round", ROOM_CODE))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("POST /guess — 400 when playerId is blank")
        void submitGuess_blankPlayerId_returns400() throws Exception {
                GuessRequest req = new GuessRequest();
                req.setPlayerId("");
                req.setPhase("GAME");
                req.setTextAnswer("Mario Kart Wii");

                mockMvc.perform(post("/api/v1/games/{code}/guess", ROOM_CODE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /guess — 202 for valid SPOT phase guess")
        void submitGuess_spotPhase_returns202() throws Exception {
                doNothing().when(gameUseCase).submitGuess(eq(ROOM_CODE), any());

                GuessRequest req = new GuessRequest();
                req.setPlayerId("player-1");
                req.setPhase("SPOT");
                req.setGuessX(100.0);
                req.setGuessY(50.0);
                req.setGuessZ(-200.0);

                mockMvc.perform(post("/api/v1/games/{code}/guess", ROOM_CODE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isAccepted());
        }

        // ── helpers ──────────────────────────────────────────────────────

        private Match buildInProgressMatch() {
                return Match.builder()
                                .id(UUID.randomUUID())
                                .roomCode(ROOM_CODE)
                                .hostId("host-1")
                                .playerIds(List.of("host-1", "player-2"))
                                .gamePack("mario-kart-wii")
                                .status(MatchStatus.IN_PROGRESS)
                                .rounds(List.of(buildRound(1, GuessPhase.GAME)))
                                .currentRoundIndex(0)
                                .build();
        }

        private Round buildRound(int number, GuessPhase phase) {
                return Round.builder()
                                .id(UUID.randomUUID())
                                .roundNumber(number)
                                .gamePackEntry(GamePackEntry.builder()
                                                .gameId("mario-kart-wii")
                                                .levelId("Luigi Circuit")
                                                .noclipHash("mkwii/beginner_course;ShareData=test")
                                                .build())
                                .currentPhase(phase)
                                .finished(false)
                                .startedAt(Instant.now().toEpochMilli())
                                .build();
        }
}
