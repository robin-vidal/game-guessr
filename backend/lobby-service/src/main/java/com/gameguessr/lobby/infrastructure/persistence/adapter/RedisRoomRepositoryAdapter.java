package com.gameguessr.lobby.infrastructure.persistence.adapter;

import com.gameguessr.lobby.domain.model.Room;
import com.gameguessr.lobby.domain.model.RoomPlayer;
import com.gameguessr.lobby.domain.model.RoomSettings;
import com.gameguessr.lobby.domain.model.RoomStatus;
import com.gameguessr.lobby.domain.port.outbound.RoomRepository;
import com.gameguessr.lobby.infrastructure.persistence.dto.RoomRedisDto;
import com.gameguessr.lobby.infrastructure.persistence.dto.RoomPlayerRedisDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Driven adapter — implements RoomRepository using Redis.
 * Rooms are stored as JSON values with key pattern: room:{code}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRoomRepositoryAdapter implements RoomRepository {

    private static final String KEY_PREFIX = "room:";
    private static final String STATUS_INDEX_PREFIX = "rooms:status:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${lobby.room.ttl-minutes:120}")
    private int ttlMinutes;

    @Override
    public Room save(Room room) {
        String key = KEY_PREFIX + room.getRoomCode();
        RoomRedisDto dto = toDto(room);
        redisTemplate.opsForValue().set(key, dto, Duration.ofMinutes(ttlMinutes));

        // Maintain status index
        redisTemplate.opsForSet().add(STATUS_INDEX_PREFIX + room.getStatus().name(), room.getRoomCode());

        // Remove from other status indexes
        for (RoomStatus status : RoomStatus.values()) {
            if (status != room.getStatus()) {
                redisTemplate.opsForSet().remove(STATUS_INDEX_PREFIX + status.name(), room.getRoomCode());
            }
        }

        return room;
    }

    @Override
    public Optional<Room> findByCode(String roomCode) {
        String key = KEY_PREFIX + roomCode;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        RoomRedisDto dto = objectMapper.convertValue(value, RoomRedisDto.class);
        return Optional.of(toDomain(dto));
    }

    @Override
    public List<Room> findByStatus(RoomStatus status) {
        String indexKey = STATUS_INDEX_PREFIX + status.name();
        Set<Object> codes = redisTemplate.opsForSet().members(indexKey);
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }

        return codes.stream()
                .map(code -> findByCode(code.toString()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String roomCode) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + roomCode));
    }

    @Override
    public void deleteByCode(String roomCode) {
        redisTemplate.delete(KEY_PREFIX + roomCode);
        for (RoomStatus status : RoomStatus.values()) {
            redisTemplate.opsForSet().remove(STATUS_INDEX_PREFIX + status.name(), roomCode);
        }
    }

    // ── Mapping ──────────────────────────────────────────────────────

    private RoomRedisDto toDto(Room room) {
        return RoomRedisDto.builder()
                .roomCode(room.getRoomCode())
                .hostId(room.getHostId())
                .isPrivate(room.isPrivate())
                .status(room.getStatus().name())
                .maxPlayers(room.getMaxPlayers())
                .roundCount(room.getSettings().getRoundCount())
                .timeLimitSeconds(room.getSettings().getTimeLimitSeconds())
                .gamePack(room.getSettings().getGamePack())
                .players(room.getPlayers().stream()
                        .map(p -> RoomPlayerRedisDto.builder()
                                .playerId(p.getPlayerId())
                                .displayName(p.getDisplayName())
                                .joinedAt(p.getJoinedAt().toString())
                                .build())
                        .toList())
                .createdAt(room.getCreatedAt().toString())
                .build();
    }

    private Room toDomain(RoomRedisDto dto) {
        List<RoomPlayer> players = dto.getPlayers() != null
                ? dto.getPlayers().stream()
                        .map(p -> RoomPlayer.builder()
                                .playerId(p.getPlayerId())
                                .displayName(p.getDisplayName())
                                .joinedAt(Instant.parse(p.getJoinedAt()))
                                .build())
                        .collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();

        return Room.builder()
                .roomCode(dto.getRoomCode())
                .hostId(dto.getHostId())
                .isPrivate(dto.isPrivate())
                .status(RoomStatus.valueOf(dto.getStatus()))
                .maxPlayers(dto.getMaxPlayers())
                .settings(RoomSettings.builder()
                        .roundCount(dto.getRoundCount())
                        .timeLimitSeconds(dto.getTimeLimitSeconds())
                        .gamePack(dto.getGamePack())
                        .build())
                .players(players)
                .createdAt(Instant.parse(dto.getCreatedAt()))
                .build();
    }
}
