package com.gameguessr.game.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Request body for {@code POST /api/v1/games/{code}/start}.
 */
@Data
@Schema(description = "Request to start a match for a given room")
public class StartMatchRequest {

    @NotBlank
    @Schema(description = "ID of the host player initiating the match", example = "user-uuid-123")
    private String hostId;

    @NotEmpty
    @Schema(description = "IDs of all players in the room", example = "[\"user-uuid-123\", \"user-uuid-456\"]")
    private List<String> playerIds;
}
