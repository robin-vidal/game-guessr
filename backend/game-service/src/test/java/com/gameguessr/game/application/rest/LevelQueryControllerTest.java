package com.gameguessr.game.application.rest;

import com.gameguessr.game.domain.port.inbound.GameUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LevelQueryController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("LevelQueryController")
class LevelQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameUseCase gameUseCase;

    @Test
    @DisplayName("GET /api/v1/games — 200 with slug and displayName")
    void getGames_returns200WithSlugAndDisplayName() throws Exception {
        when(gameUseCase.getGamePacks()).thenReturn(List.of("mario-kart-wii", "the-legend-of-zelda"));

        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("mario-kart-wii"))
                .andExpect(jsonPath("$[0].displayName").value("Mario Kart Wii"))
                .andExpect(jsonPath("$[1].slug").value("the-legend-of-zelda"))
                .andExpect(jsonPath("$[1].displayName").value("The Legend Of Zelda"));
    }

    @Test
    @DisplayName("GET /api/v1/games — 200 with empty list when no games")
    void getGames_emptyList_returns200() throws Exception {
        when(gameUseCase.getGamePacks()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/levels/autocomplete — 200 with matching level names")
    void autocompleteLevels_returns200WithMatches() throws Exception {
        when(gameUseCase.getLevelNames("circuit")).thenReturn(List.of("Luigi Circuit", "Mario Circuit"));

        mockMvc.perform(get("/api/v1/levels/autocomplete").param("q", "circuit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Luigi Circuit"))
                .andExpect(jsonPath("$[1]").value("Mario Circuit"));
    }

    @Test
    @DisplayName("GET /api/v1/levels/autocomplete — 200 with empty list when query is blank")
    void autocompleteLevels_blankQuery_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/levels/autocomplete").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(gameUseCase, never()).getLevelNames(any());
    }

    @Test
    @DisplayName("GET /api/v1/levels/autocomplete — 200 with empty list when q param missing")
    void autocompleteLevels_noParam_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/levels/autocomplete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
