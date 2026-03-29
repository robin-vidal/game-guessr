package com.gameguessr.game.application.rest;

import com.gameguessr.game.application.rest.dto.GameResponse;
import com.gameguessr.game.domain.port.inbound.GameUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Autocomplete", description = "Game and level autocomplete endpoints for guess input")
public class LevelQueryController {

    private final GameUseCase gameUseCase;

    @GetMapping("/api/v1/games")
    @Operation(summary = "List all available games", responses = {
            @ApiResponse(responseCode = "200", description = "List of game slugs and display names")
    })
    public ResponseEntity<List<GameResponse>> getGames() {
        List<GameResponse> games = gameUseCase.getGamePacks().stream()
                .map(slug -> GameResponse.builder()
                        .slug(slug)
                        .displayName(slugToDisplayName(slug))
                        .build())
                .toList();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/api/v1/levels/autocomplete")
    @Operation(summary = "Autocomplete level names", responses = {
            @ApiResponse(responseCode = "200", description = "List of matching level names (max 8)")
    })
    public ResponseEntity<List<String>> autocompleteLevels(
            @Parameter(description = "Search query (min 1 char)", example = "circuit")
            @RequestParam(required = false, defaultValue = "") String q) {

        if (q.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(gameUseCase.getLevelNames(q));
    }

    private String slugToDisplayName(String slug) {
        return Arrays.stream(slug.split("-"))
                .map(w -> w.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }
}
