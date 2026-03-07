package com.gameguessr.lobby.application.rest;

import com.gameguessr.lobby.application.rest.dto.*;
import com.gameguessr.lobby.domain.model.Room;
import com.gameguessr.lobby.domain.model.RoomSettings;
import com.gameguessr.lobby.domain.port.inbound.LobbyUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Driving adapter — exposes all lobby use cases as REST endpoints.
 * Base path: {@code /api/v1/rooms}
 */
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Lobby", description = "Room management and player lobby endpoints")
public class LobbyController {

    private final LobbyUseCase lobbyUseCase;

    /**
     * POST /api/v1/rooms
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a room", description = "Creates a new room and publishes a game.room.events Kafka event.", responses = {
            @ApiResponse(responseCode = "201", description = "Room created")
    })
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Room room = lobbyUseCase.createRoom(request.getHostId(), Boolean.TRUE.equals(request.getIsPrivate()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(room));
    }

    /**
     * GET /api/v1/rooms
     */
    @GetMapping
    @Operation(summary = "List open rooms", description = "Returns all rooms with OPEN status.", responses = {
            @ApiResponse(responseCode = "200", description = "Room list returned")
    })
    public ResponseEntity<List<RoomResponse>> listOpenRooms() {
        List<Room> rooms = lobbyUseCase.findOpenRooms();
        return ResponseEntity.ok(rooms.stream().map(this::toResponse).toList());
    }

    /**
     * GET /api/v1/rooms/{code}
     */
    @GetMapping("/{code}")
    @Operation(summary = "Get room details", description = "Returns room details including players, settings, and status.", responses = {
            @ApiResponse(responseCode = "200", description = "Room details returned"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<RoomResponse> getRoom(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code) {
        Room room = lobbyUseCase.getRoom(code);
        return ResponseEntity.ok(toResponse(room));
    }

    /**
     * PATCH /api/v1/rooms/{code}/settings
     */
    @PatchMapping("/{code}/settings")
    @Operation(summary = "Update room settings", description = "Updates round count, time limit, or game pack. Host-only.", responses = {
            @ApiResponse(responseCode = "200", description = "Settings updated"),
            @ApiResponse(responseCode = "403", description = "Not the host"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<RoomResponse> updateSettings(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code,
            @Valid @RequestBody UpdateSettingsRequest request) {

        RoomSettings settings = RoomSettings.builder()
                .roundCount(request.getRoundCount() != null ? request.getRoundCount() : 5)
                .timeLimitSeconds(request.getTimeLimitSeconds() != null ? request.getTimeLimitSeconds() : 60)
                .gamePack(request.getGamePack() != null ? request.getGamePack() : "mario-kart")
                .build();

        Room room = lobbyUseCase.updateSettings(code, request.getPlayerId(), settings);
        return ResponseEntity.ok(toResponse(room));
    }

    /**
     * POST /api/v1/rooms/{code}/join
     */
    @PostMapping("/{code}/join")
    @Operation(summary = "Join a room", description = "Adds a player to the lobby.", responses = {
            @ApiResponse(responseCode = "200", description = "Player joined"),
            @ApiResponse(responseCode = "404", description = "Room not found"),
            @ApiResponse(responseCode = "409", description = "Room full or player already in room")
    })
    public ResponseEntity<RoomResponse> joinRoom(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code,
            @Valid @RequestBody JoinRoomRequest request) {

        Room room = lobbyUseCase.joinRoom(code, request.getPlayerId(), request.getDisplayName());
        return ResponseEntity.ok(toResponse(room));
    }

    /**
     * DELETE /api/v1/rooms/{code}/leave
     */
    @DeleteMapping("/{code}/leave")
    @Operation(summary = "Leave a room", description = "Removes yourself from the lobby. If the host leaves, the room is closed.", responses = {
            @ApiResponse(responseCode = "204", description = "Player left"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<Void> leaveRoom(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code,
            @Valid @RequestBody LeaveRoomRequest request) {

        lobbyUseCase.leaveRoom(code, request.getPlayerId());
        return ResponseEntity.noContent().build();
    }

    // ── Mapping helpers ──────────────────────────────────────────────

    private RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .roomCode(room.getRoomCode())
                .hostId(room.getHostId())
                .isPrivate(room.isPrivate())
                .status(room.getStatus().name())
                .maxPlayers(room.getMaxPlayers())
                .settings(RoomResponse.SettingsResponse.builder()
                        .roundCount(room.getSettings().getRoundCount())
                        .timeLimitSeconds(room.getSettings().getTimeLimitSeconds())
                        .gamePack(room.getSettings().getGamePack())
                        .build())
                .players(room.getPlayers().stream()
                        .map(p -> RoomResponse.PlayerResponse.builder()
                                .playerId(p.getPlayerId())
                                .displayName(p.getDisplayName())
                                .joinedAt(p.getJoinedAt())
                                .build())
                        .toList())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
