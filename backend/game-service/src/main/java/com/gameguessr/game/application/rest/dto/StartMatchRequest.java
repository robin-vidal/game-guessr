package com.gameguessr.game.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/rooms/{code}/start}.
 */
@Data
@Schema(description = "Request to start a match for a given room")
public class StartMatchRequest {

    @NotBlank
    @Schema(description = "ID of the host player initiating the match", example = "user-uuid-123")
    private String hostId;
}
