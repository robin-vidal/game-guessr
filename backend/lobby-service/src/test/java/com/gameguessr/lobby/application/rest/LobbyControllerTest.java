package com.gameguessr.lobby.application.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameguessr.lobby.application.rest.dto.*;
import com.gameguessr.lobby.domain.exception.*;
import com.gameguessr.lobby.domain.model.*;
import com.gameguessr.lobby.domain.port.inbound.LobbyUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LobbyController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("LobbyController")
class LobbyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LobbyUseCase lobbyUseCase;

    private static final String ROOM_CODE = "ABC123";
    private static final String HOST_ID = "host-uuid-1";

    // ── POST /rooms ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /rooms — 201 when room created")
    void createRoom_returns201() throws Exception {
        when(lobbyUseCase.createRoom(anyString(), anyBoolean())).thenReturn(buildRoom());

        CreateRoomRequest req = new CreateRoomRequest();
        req.setHostId(HOST_ID);
        req.setIsPrivate(false);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomCode").value(ROOM_CODE))
                .andExpect(jsonPath("$.hostId").value(HOST_ID));
    }

    @Test
    @DisplayName("POST /rooms — 400 when hostId is blank")
    void createRoom_blankHostId_returns400() throws Exception {
        CreateRoomRequest req = new CreateRoomRequest();
        req.setHostId("");

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /rooms ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /rooms — 200 with list of open rooms")
    void listOpenRooms_returns200() throws Exception {
        when(lobbyUseCase.findOpenRooms()).thenReturn(List.of(buildRoom()));

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].roomCode").value(ROOM_CODE));
    }

    // ── GET /rooms/{code} ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /rooms/{code} — 200 with room details")
    void getRoom_found_returns200() throws Exception {
        when(lobbyUseCase.getRoom(ROOM_CODE)).thenReturn(buildRoom());

        mockMvc.perform(get("/api/v1/rooms/{code}", ROOM_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCode").value(ROOM_CODE))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /rooms/{code} — 404 when room not found")
    void getRoom_notFound_returns404() throws Exception {
        when(lobbyUseCase.getRoom(ROOM_CODE)).thenThrow(new RoomNotFoundException(ROOM_CODE));

        mockMvc.perform(get("/api/v1/rooms/{code}", ROOM_CODE))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /rooms/{code}/settings ─────────────────────────────────

    @Test
    @DisplayName("PATCH /rooms/{code}/settings — 200 when settings updated")
    void updateSettings_returns200() throws Exception {
        when(lobbyUseCase.updateSettings(eq(ROOM_CODE), anyString(), any()))
                .thenReturn(buildRoom());

        UpdateSettingsRequest req = new UpdateSettingsRequest();
        req.setPlayerId(HOST_ID);
        req.setRoundCount(10);

        mockMvc.perform(patch("/api/v1/rooms/{code}/settings", ROOM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /rooms/{code}/settings — 403 when not host")
    void updateSettings_notHost_returns403() throws Exception {
        when(lobbyUseCase.updateSettings(eq(ROOM_CODE), anyString(), any()))
                .thenThrow(new NotHostException("other-player", ROOM_CODE));

        UpdateSettingsRequest req = new UpdateSettingsRequest();
        req.setPlayerId("other-player");

        mockMvc.perform(patch("/api/v1/rooms/{code}/settings", ROOM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── POST /rooms/{code}/join ───────────────────────────────────────

    @Test
    @DisplayName("POST /rooms/{code}/join — 200 when player joined")
    void joinRoom_returns200() throws Exception {
        when(lobbyUseCase.joinRoom(eq(ROOM_CODE), anyString(), anyString()))
                .thenReturn(buildRoom());

        JoinRoomRequest req = new JoinRoomRequest();
        req.setPlayerId("player-2");
        req.setDisplayName("Player2");

        mockMvc.perform(post("/api/v1/rooms/{code}/join", ROOM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /rooms/{code}/join — 409 when room is full")
    void joinRoom_full_returns409() throws Exception {
        when(lobbyUseCase.joinRoom(eq(ROOM_CODE), anyString(), anyString()))
                .thenThrow(new RoomFullException(ROOM_CODE));

        JoinRoomRequest req = new JoinRoomRequest();
        req.setPlayerId("player-9");
        req.setDisplayName("Player9");

        mockMvc.perform(post("/api/v1/rooms/{code}/join", ROOM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /rooms/{code}/join — 409 when player already in room")
    void joinRoom_alreadyInRoom_returns409() throws Exception {
        when(lobbyUseCase.joinRoom(eq(ROOM_CODE), anyString(), anyString()))
                .thenThrow(new PlayerAlreadyInRoomException(HOST_ID, ROOM_CODE));

        JoinRoomRequest req = new JoinRoomRequest();
        req.setPlayerId(HOST_ID);
        req.setDisplayName("Host");

        mockMvc.perform(post("/api/v1/rooms/{code}/join", ROOM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /rooms/{code}/join — 400 when playerId is blank")
    void joinRoom_blankPlayerId_returns400() throws Exception {
        JoinRoomRequest req = new JoinRoomRequest();
        req.setPlayerId("");
        req.setDisplayName("Player");

        mockMvc.perform(post("/api/v1/rooms/{code}/join", ROOM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /rooms/{code}/leave ────────────────────────────────────

    @Test
    @DisplayName("DELETE /rooms/{code}/leave — 204 when player left")
    void leaveRoom_returns204() throws Exception {
        doNothing().when(lobbyUseCase).leaveRoom(eq(ROOM_CODE), anyString());

        LeaveRoomRequest req = new LeaveRoomRequest();
        req.setPlayerId(HOST_ID);

        mockMvc.perform(delete("/api/v1/rooms/{code}/leave", ROOM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /rooms/{code}/leave — 404 when room not found")
    void leaveRoom_notFound_returns404() throws Exception {
        doThrow(new RoomNotFoundException(ROOM_CODE))
                .when(lobbyUseCase).leaveRoom(eq(ROOM_CODE), anyString());

        LeaveRoomRequest req = new LeaveRoomRequest();
        req.setPlayerId(HOST_ID);

        mockMvc.perform(delete("/api/v1/rooms/{code}/leave", ROOM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Room buildRoom() {
        List<RoomPlayer> players = new ArrayList<>();
        players.add(RoomPlayer.builder()
                .playerId(HOST_ID)
                .displayName("Host")
                .joinedAt(Instant.now())
                .build());

        return Room.builder()
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .isPrivate(false)
                .status(RoomStatus.OPEN)
                .settings(RoomSettings.builder()
                        .roundCount(5)
                        .timeLimitSeconds(60)
                        .gamePack("mario-kart-wii")
                        .build())
                .maxPlayers(8)
                .players(players)
                .createdAt(Instant.now())
                .build();
    }
}
