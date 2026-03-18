package com.gameguessr.lobby.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code DELETE /api/v1/rooms/{code}/leave}.
 */
@Data
@Schema(description = "Request to leave a room")
public class LeaveRoomRequest {

    @NotBlank
    @Schema(description = "ID of the player leaving", example = "user-uuid-456")
    private String playerId;
}
