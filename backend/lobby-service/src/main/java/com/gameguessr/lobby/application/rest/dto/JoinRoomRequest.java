package com.gameguessr.lobby.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/rooms/{code}/join}.
 */
@Data
@Schema(description = "Request to join a room")
public class JoinRoomRequest {

    @NotBlank
    @Schema(description = "ID of the player joining", example = "user-uuid-456")
    private String playerId;

    @NotBlank
    @Schema(description = "Display name of the player", example = "Player1")
    private String displayName;
}
