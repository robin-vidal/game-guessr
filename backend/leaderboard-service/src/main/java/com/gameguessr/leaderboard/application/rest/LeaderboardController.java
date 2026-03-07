package com.gameguessr.leaderboard.application.rest;

import com.gameguessr.leaderboard.application.rest.dto.LeaderboardResponse;
import com.gameguessr.leaderboard.domain.model.LeaderboardEntry;
import com.gameguessr.leaderboard.domain.port.inbound.LeaderboardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Driving adapter — exposes leaderboard query endpoints.
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Real-time player ranking endpoints")
public class LeaderboardController {

    private final LeaderboardUseCase leaderboardUseCase;

    @GetMapping("/global")
    @Operation(summary = "Get global leaderboard", description = "Returns top players across all games.", responses = {
            @ApiResponse(responseCode = "200", description = "Leaderboard returned")
    })
    public ResponseEntity<LeaderboardResponse> getGlobalLeaderboard(
            @Parameter(description = "Number of top entries to return", example = "100") @RequestParam(defaultValue = "100") int top) {

        List<LeaderboardEntry> entries = leaderboardUseCase.getGlobalLeaderboard(top);
        return ResponseEntity.ok(toResponse("global", "all", entries));
    }

    @GetMapping("/room/{code}")
    @Operation(summary = "Get room leaderboard", description = "Returns the current ranking within a specific room.", responses = {
            @ApiResponse(responseCode = "200", description = "Room leaderboard returned")
    })
    public ResponseEntity<LeaderboardResponse> getRoomLeaderboard(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code) {

        List<LeaderboardEntry> entries = leaderboardUseCase.getRoomLeaderboard(code);
        return ResponseEntity.ok(toResponse("room", code, entries));
    }

    private LeaderboardResponse toResponse(String type, String identifier,
            List<LeaderboardEntry> entries) {
        return LeaderboardResponse.builder()
                .leaderboardType(type)
                .identifier(identifier)
                .entries(entries.stream()
                        .map(e -> LeaderboardResponse.EntryResponse.builder()
                                .rank(e.getRank())
                                .playerId(e.getPlayerId())
                                .score(e.getScore())
                                .build())
                        .toList())
                .build();
    }
}
