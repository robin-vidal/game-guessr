package com.gameguessr.lobby.infrastructure.persistence.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameguessr.lobby.domain.model.*;
import com.gameguessr.lobby.infrastructure.persistence.dto.RoomRedisDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisRoomRepositoryAdapter")
class RedisRoomRepositoryAdapterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @Mock
    private SetOperations<String, Object> setOps;

    @InjectMocks
    private RedisRoomRepositoryAdapter adapter;

    private static final String ROOM_CODE = "ABC123";
    private static final String HOST_ID = "host-uuid-1";
    private static final int TTL_MINUTES = 120;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "ttlMinutes", TTL_MINUTES);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    @DisplayName("save — stores room with TTL and updates status index")
    void save_storesRoomWithTtlAndIndex() {
        Room room = buildRoom(RoomStatus.OPEN);

        Room result = adapter.save(room);

        verify(valueOps).set(eq("room:" + ROOM_CODE), any(), eq(Duration.ofMinutes(TTL_MINUTES)));
        verify(setOps).add("rooms:status:OPEN", ROOM_CODE);
        assertThat(result).isSameAs(room);
    }

    @Test
    @DisplayName("save — removes room code from other status indexes")
    void save_removesFromOtherStatusIndexes() {
        Room room = buildRoom(RoomStatus.OPEN);

        adapter.save(room);

        for (RoomStatus status : RoomStatus.values()) {
            if (status != RoomStatus.OPEN) {
                verify(setOps).remove("rooms:status:" + status.name(), ROOM_CODE);
            }
        }
    }

    @Test
    @DisplayName("findByCode — returns mapped domain when found")
    void findByCode_found_returnsDomain() {
        Object rawValue = new Object();
        RoomRedisDto dto = buildDto();

        when(valueOps.get("room:" + ROOM_CODE)).thenReturn(rawValue);
        when(objectMapper.convertValue(rawValue, RoomRedisDto.class)).thenReturn(dto);

        Optional<Room> result = adapter.findByCode(ROOM_CODE);

        assertThat(result).isPresent();
        assertThat(result.get().getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(result.get().getHostId()).isEqualTo(HOST_ID);
        assertThat(result.get().getStatus()).isEqualTo(RoomStatus.OPEN);
    }

    @Test
    @DisplayName("findByCode — returns empty when key not found")
    void findByCode_notFound_returnsEmpty() {
        when(valueOps.get("room:" + ROOM_CODE)).thenReturn(null);

        Optional<Room> result = adapter.findByCode(ROOM_CODE);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByStatus — returns rooms matching status")
    void findByStatus_returnsMatchingRooms() {
        Object rawValue = new Object();
        RoomRedisDto dto = buildDto();

        when(setOps.members("rooms:status:OPEN")).thenReturn(Set.of(ROOM_CODE));
        when(valueOps.get("room:" + ROOM_CODE)).thenReturn(rawValue);
        when(objectMapper.convertValue(rawValue, RoomRedisDto.class)).thenReturn(dto);

        List<Room> result = adapter.findByStatus(RoomStatus.OPEN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomCode()).isEqualTo(ROOM_CODE);
    }

    @Test
    @DisplayName("findByStatus — returns empty list when no rooms in status index")
    void findByStatus_emptyIndex_returnsEmptyList() {
        when(setOps.members("rooms:status:OPEN")).thenReturn(null);

        List<Room> result = adapter.findByStatus(RoomStatus.OPEN);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByCode — delegates to redisTemplate.hasKey")
    void existsByCode_delegates() {
        when(redisTemplate.hasKey("room:" + ROOM_CODE)).thenReturn(true);

        assertThat(adapter.existsByCode(ROOM_CODE)).isTrue();
    }

    @Test
    @DisplayName("deleteByCode — deletes key and removes from all status indexes")
    void deleteByCode_deletesKeyAndCleansIndexes() {
        adapter.deleteByCode(ROOM_CODE);

        verify(redisTemplate).delete("room:" + ROOM_CODE);
        for (RoomStatus status : RoomStatus.values()) {
            verify(setOps).remove("rooms:status:" + status.name(), ROOM_CODE);
        }
    }

    @Test
    @DisplayName("findByStatus — returns empty list when index is empty set")
    void findByStatus_emptySet_returnsEmptyList() {
        when(setOps.members("rooms:status:OPEN")).thenReturn(Set.of());

        List<Room> result = adapter.findByStatus(RoomStatus.OPEN);

        assertThat(result).isEmpty();
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Room buildRoom(RoomStatus status) {
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
                .status(status)
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

    private RoomRedisDto buildDto() {
        return RoomRedisDto.builder()
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .isPrivate(false)
                .status("OPEN")
                .maxPlayers(8)
                .roundCount(5)
                .timeLimitSeconds(60)
                .gamePack("mario-kart-wii")
                .players(List.of())
                .createdAt(Instant.now().toString())
                .build();
    }
}
