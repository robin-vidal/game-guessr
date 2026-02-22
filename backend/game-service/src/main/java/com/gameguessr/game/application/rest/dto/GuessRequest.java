package com.gameguessr.game.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/rooms/{code}/guess}.
 */
@Data
@Schema(description = "A player's guess for the current round phase")
public class GuessRequest {

    @NotBlank
    @Schema(description = "Player's user ID", example = "user-uuid-456")
    private String playerId;

    @NotNull
    @Pattern(regexp = "GAME|LEVEL|SPOT", message = "Phase must be GAME, LEVEL, or SPOT")
    @Schema(description = "Which phase this guess targets", example = "GAME", allowableValues = { "GAME", "LEVEL",
            "SPOT" })
    private String phase;

    @Schema(description = "Text answer for GAME or LEVEL phase", example = "Mario Kart 8")
    private String textAnswer;

    @Schema(description = "Guessed X coordinate (SPOT phase only)", example = "0.0")
    private Double guessX;

    @Schema(description = "Guessed Y coordinate (SPOT phase only)", example = "0.0")
    private Double guessY;

    @Schema(description = "Guessed Z coordinate (SPOT phase only)", example = "0.0")
    private Double guessZ;
}
