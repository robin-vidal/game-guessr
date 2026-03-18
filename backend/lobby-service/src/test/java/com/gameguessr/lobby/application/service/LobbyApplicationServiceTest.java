package com.gameguessr.lobby.application.service;

import com.gameguessr.lobby.domain.exception.NotHostException;
import com.gameguessr.lobby.domain.exception.PlayerAlreadyInRoomException;
import com.gameguessr.lobby.domain.exception.RoomFullException;
import com.gameguessr.lobby.domain.exception.RoomNotFoundException;
import com.gameguessr.lobby.domain.model.*;
import com.gameguessr.lobby.domain.port.outbound.LobbyEventPublisher;
import com.gameguessr.lobby.domain.port.outbound.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LobbyApplicationService")
class LobbyApplicationServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private LobbyEventPublisher lobbyEventPublisher;

    @InjectMocks
    private LobbyApplicationService service;

    private static final String HOST_ID = "host-uuid-1";
    private static final String ROOM_CODE = "ABC123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "codeLength", 6);
        ReflectionTestUtils.setField(service, "defaultMaxPlayers", 8);
    }

    // ── createRoom ────────────────────────────────────────────────────

    @Test
    @DisplayName("createRoom — creates room and publishes event")
    void createRoom_success() {
        when(roomRepository.existsByCode(anyString())).thenReturn(false);
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Room result = service.createRoom(HOST_ID, false);

        assertThat(result.getHostId()).isEqualTo(HOST_ID);
        assertThat(result.getStatus()).isEqualTo(RoomStatus.OPEN);
        assertThat(result.getPlayers()).hasSize(1);
        assertThat(result.getRoomCode()).hasSize(6);
        verify(lobbyEventPublisher).publishRoomCreated(any(Room.class));
    }

    @Test
    @DisplayName("createRoom — generates unique room code")
    void createRoom_uniqueCode() {
        // First call: code exists, second call: code is unique
        when(roomRepository.existsByCode(anyString())).thenReturn(true, false);
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Room result = service.createRoom(HOST_ID, true);

        assertThat(result.isPrivate()).isTrue();
        verify(roomRepository, atLeast(2)).existsByCode(anyString());
    }

    // ── joinRoom ────────────────────────────────────────────────────

    @Test
    @DisplayName("joinRoom — adds player successfully")
    void joinRoom_success() {
        Room room = buildRoom(RoomStatus.OPEN, 1);
        when(roomRepository.findByCode(ROOM_CODE)).thenReturn(Optional.of(room));
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Room result = service.joinRoom(ROOM_CODE, "player-2", "Player2");

        assertThat(result.getPlayers()).hasSize(2);
    }

    @Test
    @DisplayName("joinRoom — throws when room is full")
    void joinRoom_full_throws() {
        Room room = buildRoom(RoomStatus.FULL, 8);
        when(roomRepository.findByCode(ROOM_CODE)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.joinRoom(ROOM_CODE, "player-9", "Player9"))
                .isInstanceOf(RoomFullException.class);
    }

    @Test
    @DisplayName("joinRoom — throws when player already in room")
    void joinRoom_duplicate_throws() {
        Room room = buildRoom(RoomStatus.OPEN, 1);
        when(roomRepository.findByCode(ROOM_CODE)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.joinRoom(ROOM_CODE, HOST_ID, "Host"))
                .isInstanceOf(PlayerAlreadyInRoomException.class);
    }

    // ── leaveRoom ────────────────────────────────────────────────────

    @Test
    @DisplayName("leaveRoom — host leaving closes room")
    void leaveRoom_host_closesRoom() {
        Room room = buildRoom(RoomStatus.OPEN, 2);
        when(roomRepository.findByCode(ROOM_CODE)).thenReturn(Optional.of(room));
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.leaveRoom(ROOM_CODE, HOST_ID);

        verify(roomRepository).save(argThat(r -> r.getStatus() == RoomStatus.CLOSED));
    }

    @Test
    @DisplayName("leaveRoom — non-host player removed")
    void leaveRoom_player_removed() {
        Room room = buildRoom(RoomStatus.OPEN, 2);
        when(roomRepository.findByCode(ROOM_CODE)).thenReturn(Optional.of(room));
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.leaveRoom(ROOM_CODE, "player-1");

        verify(roomRepository).save(argThat(r -> r.getPlayers().size() == 1));
    }

    // ── updateSettings ────────────────────────────────────────────────

    @Test
    @DisplayName("updateSettings — host can update settings")
    void updateSettings_host_success() {
        Room room = buildRoom(RoomStatus.OPEN, 1);
        when(roomRepository.findByCode(ROOM_CODE)).thenReturn(Optional.of(room));
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RoomSettings newSettings = RoomSettings.builder()
                .roundCount(10)
                .timeLimitSeconds(30)
                .gamePack("zelda")
                .build();

        Room result = service.updateSettings(ROOM_CODE, HOST_ID, newSettings);

        assertThat(result.getSettings().getRoundCount()).isEqualTo(10);
        assertThat(result.getSettings().getTimeLimitSeconds()).isEqualTo(30);
    }

    @Test
    @DisplayName("updateSettings — non-host throws NotHostException")
    void updateSettings_notHost_throws() {
        Room room = buildRoom(RoomStatus.OPEN, 1);
        when(roomRepository.findByCode(ROOM_CODE)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.updateSettings(ROOM_CODE, "other-player",
                RoomSettings.builder().build()))
                .isInstanceOf(NotHostException.class);
    }

    // ── getRoom ────────────────────────────────────────────────────

    @Test
    @DisplayName("getRoom — throws when not found")
    void getRoom_notFound_throws() {
        when(roomRepository.findByCode(ROOM_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRoom(ROOM_CODE))
                .isInstanceOf(RoomNotFoundException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Room buildRoom(RoomStatus status, int playerCount) {
        List<RoomPlayer> players = new ArrayList<>();
        players.add(RoomPlayer.builder()
                .playerId(HOST_ID)
                .displayName("Host")
                .joinedAt(Instant.now())
                .build());

        for (int i = 1; i < playerCount; i++) {
            players.add(RoomPlayer.builder()
                    .playerId("player-" + i)
                    .displayName("Player" + i)
                    .joinedAt(Instant.now())
                    .build());
        }

        return Room.builder()
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .isPrivate(false)
                .status(status)
                .settings(RoomSettings.builder().build())
                .maxPlayers(8)
                .players(players)
                .createdAt(Instant.now())
                .build();
    }
}
