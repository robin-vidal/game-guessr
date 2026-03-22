package com.gameguessr.leaderboard.application.rest;

import com.gameguessr.leaderboard.domain.model.LeaderboardEntry;
import com.gameguessr.leaderboard.domain.port.inbound.LeaderboardUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaderboardController.class)
@DisplayName("LeaderboardController")
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardUseCase leaderboardUseCase;

    private static final String ROOM_CODE = "ABC123";

    // ── GET /leaderboard/global ───────────────────────────────────────

    @Test
    @DisplayName("GET /leaderboard/global — 200 with entries")
    void getGlobalLeaderboard_returns200() throws Exception {
        List<LeaderboardEntry> entries = List.of(
                LeaderboardEntry.builder().playerId("p1").score(3000).rank(1).build(),
                LeaderboardEntry.builder().playerId("p2").score(2000).rank(2).build());
        when(leaderboardUseCase.getGlobalLeaderboard(100)).thenReturn(entries);

        mockMvc.perform(get("/api/v1/leaderboard/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderboardType").value("global"))
                .andExpect(jsonPath("$.identifier").value("all"))
                .andExpect(jsonPath("$.entries").isArray())
                .andExpect(jsonPath("$.entries.length()").value(2));
    }

    @Test
    @DisplayName("GET /leaderboard/global?top=10 — delegates with top parameter")
    void getGlobalLeaderboard_withTopParam_delegatesCorrectly() throws Exception {
        List<LeaderboardEntry> entries = List.of(
                LeaderboardEntry.builder().playerId("p1").score(3000).rank(1).build());
        when(leaderboardUseCase.getGlobalLeaderboard(10)).thenReturn(entries);

        mockMvc.perform(get("/api/v1/leaderboard/global").param("top", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries.length()").value(1));

        verify(leaderboardUseCase).getGlobalLeaderboard(10);
    }

    @Test
    @DisplayName("GET /leaderboard/global — 200 with empty entries")
    void getGlobalLeaderboard_empty_returns200() throws Exception {
        when(leaderboardUseCase.getGlobalLeaderboard(100)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/leaderboard/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries").isEmpty());
    }

    @Test
    @DisplayName("GET /leaderboard/global — entry has correct rank, playerId, score fields")
    void getGlobalLeaderboard_entryMapping() throws Exception {
        List<LeaderboardEntry> entries = List.of(
                LeaderboardEntry.builder().playerId("player-1").score(2500.0).rank(1).build());
        when(leaderboardUseCase.getGlobalLeaderboard(100)).thenReturn(entries);

        mockMvc.perform(get("/api/v1/leaderboard/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].rank").value(1))
                .andExpect(jsonPath("$.entries[0].playerId").value("player-1"))
                .andExpect(jsonPath("$.entries[0].score").value(2500.0));
    }

    // ── GET /leaderboard/room/{code} ─────────────────────────────────

    @Test
    @DisplayName("GET /leaderboard/room/{code} — 200 with room entries")
    void getRoomLeaderboard_returns200() throws Exception {
        List<LeaderboardEntry> entries = List.of(
                LeaderboardEntry.builder().playerId("p1").score(1500).rank(1).build());
        when(leaderboardUseCase.getRoomLeaderboard(ROOM_CODE)).thenReturn(entries);

        mockMvc.perform(get("/api/v1/leaderboard/room/{code}", ROOM_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaderboardType").value("room"))
                .andExpect(jsonPath("$.identifier").value(ROOM_CODE))
                .andExpect(jsonPath("$.entries[0].playerId").value("p1"));
    }

    @Test
    @DisplayName("GET /leaderboard/room/{code} — 200 with empty room leaderboard")
    void getRoomLeaderboard_empty_returns200() throws Exception {
        when(leaderboardUseCase.getRoomLeaderboard(ROOM_CODE)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/leaderboard/room/{code}", ROOM_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries").isEmpty());
    }
}
