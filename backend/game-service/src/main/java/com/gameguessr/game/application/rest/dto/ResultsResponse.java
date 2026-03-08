package com.gameguessr.game.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response for {@code GET /api/v1/rooms/{code}/results}.
 * Reveals the true spawn coordinates now that the match is finished.
 */
@Data
@Builder
@Schema(description = "Final match results including true spawn coordinates per round")
public class ResultsResponse {

    @Schema(description = "Room code", example = "ABC123")
    private String roomCode;

    @Schema(description = "Final status of the match", example = "FINISHED")
    private String matchStatus;

    private List<RoundResult> rounds;

    @Data
    @Builder
    @Schema(description = "Per-round result revealing the true location")
    public static class RoundResult {

        private int roundNumber;
        private String gameId;
        private String levelId;

        @Schema(description = "True spawn X coordinate")
        private double trueSpawnX;

        @Schema(description = "True spawn Y coordinate")
        private double trueSpawnY;

        @Schema(description = "True spawn Z coordinate")
        private double trueSpawnZ;

        private boolean finished;
    }
}
