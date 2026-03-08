package com.gameguessr.lobby.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/rooms}.
 */
@Data
@Schema(description = "Request to create a new room")
public class CreateRoomRequest {

    @NotBlank
    @Schema(description = "ID of the host player creating the room", example = "user-uuid-123")
    private String hostId;

    @Schema(description = "Whether the room is private (invite-only)", example = "false")
    private Boolean isPrivate;
}
