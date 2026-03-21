package com.gameguessr.lobby.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Room")
class RoomTest {

    private static final String HOST_ID = "host-uuid-1";
    private static final String ROOM_CODE = "ABC123";

    @Test
    @DisplayName("isFull() — false when player count is below maxPlayers")
    void isFull_belowMax_returnsFalse() {
        Room room = buildRoom(RoomStatus.OPEN, 1, 8);
        assertThat(room.isFull()).isFalse();
    }

    @Test
    @DisplayName("isFull() — true when player count equals maxPlayers")
    void isFull_atMax_returnsTrue() {
        Room room = buildRoom(RoomStatus.FULL, 8, 8);
        assertThat(room.isFull()).isTrue();
    }

    @Test
    @DisplayName("hasPlayer() — true when player exists in room")
    void hasPlayer_existing_returnsTrue() {
        Room room = buildRoom(RoomStatus.OPEN, 2, 8);
        assertThat(room.hasPlayer("player-1")).isTrue();
    }

    @Test
    @DisplayName("hasPlayer() — false when player not in room")
    void hasPlayer_notPresent_returnsFalse() {
        Room room = buildRoom(RoomStatus.OPEN, 1, 8);
        assertThat(room.hasPlayer("unknown-player")).isFalse();
    }

    @Test
    @DisplayName("isHost() — true when playerId matches hostId")
    void isHost_hostPlayer_returnsTrue() {
        Room room = buildRoom(RoomStatus.OPEN, 1, 8);
        assertThat(room.isHost(HOST_ID)).isTrue();
    }

    @Test
    @DisplayName("isHost() — false when playerId does not match hostId")
    void isHost_notHost_returnsFalse() {
        Room room = buildRoom(RoomStatus.OPEN, 1, 8);
        assertThat(room.isHost("other-player")).isFalse();
    }

    @Test
    @DisplayName("canJoin() — true when room is OPEN and not full")
    void canJoin_openNotFull_returnsTrue() {
        Room room = buildRoom(RoomStatus.OPEN, 1, 8);
        assertThat(room.canJoin()).isTrue();
    }

    @Test
    @DisplayName("canJoin() — false when room status is FULL")
    void canJoin_statusFull_returnsFalse() {
        Room room = buildRoom(RoomStatus.FULL, 8, 8);
        assertThat(room.canJoin()).isFalse();
    }

    @Test
    @DisplayName("canJoin() — false when room is OPEN but player count equals maxPlayers")
    void canJoin_openButPlayerCountAtMax_returnsFalse() {
        Room room = buildRoom(RoomStatus.OPEN, 8, 8);
        assertThat(room.canJoin()).isFalse();
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Room buildRoom(RoomStatus status, int playerCount, int maxPlayers) {
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
                .maxPlayers(maxPlayers)
                .players(players)
                .createdAt(Instant.now())
                .build();
    }
}
