package com.gameguessr.scoring.application.rest;

import com.gameguessr.scoring.application.rest.dto.ScoresResponse;
import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.domain.port.inbound.ScoringUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Driving adapter — exposes scoring query endpoints.
 */
@RestController
@RequestMapping("/api/v1/scoring")
@RequiredArgsConstructor
@Tag(name = "Scoring", description = "Score query endpoints")
public class ScoringController {

    private final ScoringUseCase scoringUseCase;

    @GetMapping("/{roomCode}/rounds/{roundNumber}")
    @Operation(summary = "Get round scores", description = "Returns all scores for a specific round.", responses = {
            @ApiResponse(responseCode = "200", description = "Scores returned")
    })
    public ResponseEntity<ScoresResponse> getRoundScores(
            @Parameter(description = "Room code") @PathVariable String roomCode,
            @Parameter(description = "Round number") @PathVariable int roundNumber) {

        List<Score> scores = scoringUseCase.getRoundScores(roomCode, roundNumber);
        return ResponseEntity.ok(toResponse(roomCode, scores));
    }

    @GetMapping("/{roomCode}")
    @Operation(summary = "Get match scores", description = "Returns all scores for an entire match.", responses = {
            @ApiResponse(responseCode = "200", description = "Scores returned")
    })
    public ResponseEntity<ScoresResponse> getMatchScores(
            @Parameter(description = "Room code") @PathVariable String roomCode) {

        List<Score> scores = scoringUseCase.getMatchScores(roomCode);
        return ResponseEntity.ok(toResponse(roomCode, scores));
    }

    private ScoresResponse toResponse(String roomCode, List<Score> scores) {
        return ScoresResponse.builder()
                .roomCode(roomCode)
                .scores(scores.stream()
                        .map(s -> ScoresResponse.ScoreItem.builder()
                                .id(s.getId())
                                .roundNumber(s.getRoundNumber())
                                .playerId(s.getPlayerId())
                                .phase(s.getPhase())
                                .points(s.getPoints())
                                .correct(s.isCorrect())
                                .timeBonusMs(s.getTimeBonusMs())
                                .createdAt(s.getCreatedAt())
                                .build())
                        .toList())
                .build();
    }
}
