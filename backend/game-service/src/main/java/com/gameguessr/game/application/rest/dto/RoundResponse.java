package com.gameguessr.game.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Response for {@code GET /api/v1/games/{code}/round}.
 * Intentionally omits the true spawn coordinates.
 */
@Data
@Builder
@Schema(description = "Current round information (spawn coordinates withheld)")
public class RoundResponse {

    @Schema(description = "1-based round number", example = "1")
    private int roundNumber;

    @Schema(description = "Game identifier", example = "mario-kart-8")
    private String gameId;

    @Schema(description = "Level / track identifier (displayed to player)", example = "TBD-1")
    private String levelId;

    @Schema(description = "Current phase of this round", example = "GAME", allowableValues = { "GAME", "LEVEL",
            "SPOT" })
    private String currentPhase;

    @Schema(description = "Noclip hash for the 3D viewer")
    private String noclipHash;

    @Schema(description = "Whether this round has been completed")
    private boolean finished;

    @Schema(description = "Epoch milliseconds when this round started")
    private long startedAt;
}
