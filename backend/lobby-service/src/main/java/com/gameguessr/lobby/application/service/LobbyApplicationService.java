package com.gameguessr.lobby.application.service;

import com.gameguessr.lobby.domain.exception.NotHostException;
import com.gameguessr.lobby.domain.exception.PlayerAlreadyInRoomException;
import com.gameguessr.lobby.domain.exception.RoomFullException;
import com.gameguessr.lobby.domain.exception.RoomNotFoundException;
import com.gameguessr.lobby.domain.model.Room;
import com.gameguessr.lobby.domain.model.RoomPlayer;
import com.gameguessr.lobby.domain.model.RoomSettings;
import com.gameguessr.lobby.domain.model.RoomStatus;
import com.gameguessr.lobby.domain.port.inbound.LobbyUseCase;
import com.gameguessr.lobby.domain.port.outbound.LobbyEventPublisher;
import com.gameguessr.lobby.domain.port.outbound.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Application service implementing all lobby use cases.
 * Orchestrates domain logic and delegates to outbound ports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyApplicationService implements LobbyUseCase {

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RoomRepository roomRepository;
    private final LobbyEventPublisher lobbyEventPublisher;

    @Value("${lobby.room.code-length:6}")
    private int codeLength;

    @Value("${lobby.room.max-players:8}")
    private int defaultMaxPlayers;

    @Override
    public Room createRoom(String hostId, boolean isPrivate) {
        String roomCode = generateUniqueCode();

        RoomPlayer host = RoomPlayer.builder()
                .playerId(hostId)
                .displayName("Host")
                .joinedAt(Instant.now())
                .build();

        List<RoomPlayer> players = new ArrayList<>();
        players.add(host);

        Room room = Room.builder()
                .roomCode(roomCode)
                .hostId(hostId)
                .isPrivate(isPrivate)
                .status(RoomStatus.OPEN)
                .settings(RoomSettings.builder().build())
                .maxPlayers(defaultMaxPlayers)
                .players(players)
                .createdAt(Instant.now())
                .build();

        Room saved = roomRepository.save(room);

        // Publish room.created event → consumed by Game Service
        lobbyEventPublisher.publishRoomCreated(saved);

        log.info("Room {} created by host {} (private={})", roomCode, hostId, isPrivate);
        return saved;
    }

    @Override
    public Room getRoom(String roomCode) {
        return findRoom(roomCode);
    }

    @Override
    public Room updateSettings(String roomCode, String playerId, RoomSettings settings) {
        Room room = findRoom(roomCode);

        if (!room.isHost(playerId)) {
            throw new NotHostException(playerId, roomCode);
        }

        Room updated = room.withSettings(settings);
        Room saved = roomRepository.save(updated);

        log.info("Room {} settings updated by host {}", roomCode, playerId);
        return saved;
    }

    @Override
    public Room joinRoom(String roomCode, String playerId, String displayName) {
        Room room = findRoom(roomCode);

        if (!room.canJoin()) {
            throw new RoomFullException(roomCode);
        }

        if (room.hasPlayer(playerId)) {
            throw new PlayerAlreadyInRoomException(playerId, roomCode);
        }

        RoomPlayer player = RoomPlayer.builder()
                .playerId(playerId)
                .displayName(displayName)
                .joinedAt(Instant.now())
                .build();

        List<RoomPlayer> updatedPlayers = new ArrayList<>(room.getPlayers());
        updatedPlayers.add(player);

        RoomStatus newStatus = updatedPlayers.size() >= room.getMaxPlayers()
                ? RoomStatus.FULL
                : RoomStatus.OPEN;

        Room updated = room.withPlayers(updatedPlayers).withStatus(newStatus);
        Room saved = roomRepository.save(updated);

        log.info("Player {} ({}) joined room {}", playerId, displayName, roomCode);
        return saved;
    }

    @Override
    public void leaveRoom(String roomCode, String playerId) {
        Room room = findRoom(roomCode);

        if (room.isHost(playerId)) {
            // Host leaving closes the room
            Room closed = room.withStatus(RoomStatus.CLOSED);
            roomRepository.save(closed);
            log.info("Host {} left room {} — room CLOSED", playerId, roomCode);
            return;
        }

        List<RoomPlayer> updatedPlayers = new ArrayList<>(room.getPlayers());
        updatedPlayers.removeIf(p -> p.getPlayerId().equals(playerId));

        RoomStatus newStatus = updatedPlayers.size() < room.getMaxPlayers()
                ? RoomStatus.OPEN
                : room.getStatus();

        Room updated = room.withPlayers(updatedPlayers).withStatus(newStatus);
        roomRepository.save(updated);

        log.info("Player {} left room {}", playerId, roomCode);
    }

    @Override
    public List<Room> findOpenRooms() {
        return roomRepository.findByStatus(RoomStatus.OPEN);
    }

    // ── Private helpers ──────────────────────────────────────────────

    private Room findRoom(String roomCode) {
        return roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new RoomNotFoundException(roomCode));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (roomRepository.existsByCode(code));
        return code;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
