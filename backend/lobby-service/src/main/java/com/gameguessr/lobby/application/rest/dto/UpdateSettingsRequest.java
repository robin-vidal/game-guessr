package com.gameguessr.lobby.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code PATCH /api/v1/rooms/{code}/settings}.
 */
@Data
@Schema(description = "Request to update room settings (host-only)")
public class UpdateSettingsRequest {

    @NotBlank
    @Schema(description = "ID of the player requesting changes (must be host)", example = "user-uuid-123")
    private String playerId;

    @Schema(description = "Number of rounds per match", example = "5")
    private Integer roundCount;

    @Schema(description = "Time limit per round in seconds", example = "60")
    private Integer timeLimitSeconds;

    @Schema(description = "Game pack identifier", example = "mario-kart")
    private String gamePack;
}
