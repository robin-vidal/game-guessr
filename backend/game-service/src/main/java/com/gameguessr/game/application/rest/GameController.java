package com.gameguessr.game.application.rest;

import com.gameguessr.game.application.rest.dto.GuessRequest;
import com.gameguessr.game.application.rest.dto.ResultsResponse;
import com.gameguessr.game.application.rest.dto.RoundResponse;
import com.gameguessr.game.application.rest.dto.StartMatchRequest;
import com.gameguessr.game.domain.model.Guess;
import com.gameguessr.game.domain.model.GuessPhase;
import com.gameguessr.game.domain.model.Match;
import com.gameguessr.game.domain.model.Round;
import com.gameguessr.game.domain.port.inbound.GameUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms/{code}")
@RequiredArgsConstructor
@Tag(name = "Game", description = "Game lifecycle and guess submission endpoints")
public class GameController {

    private final GameUseCase gameUseCase;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start a match", responses = {
            @ApiResponse(responseCode = "201", description = "Match started"),
            @ApiResponse(responseCode = "409", description = "Match already started"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<Void> startMatch(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code,
            @Valid @RequestBody StartMatchRequest request) {

        gameUseCase.startMatch(code, request.getHostId(), request.getPlayerIds());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/round")
    @Operation(summary = "Get current round", responses = {
            @ApiResponse(responseCode = "200", description = "Round info returned"),
            @ApiResponse(responseCode = "404", description = "Room or match not found")
    })
    public ResponseEntity<RoundResponse> getCurrentRound(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code) {

        Round round = gameUseCase.getCurrentRound(code);
        return ResponseEntity.ok(toRoundResponse(round));
    }

    @PostMapping("/guess")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Submit a guess", responses = {
            @ApiResponse(responseCode = "202", description = "Guess accepted and published"),
            @ApiResponse(responseCode = "400", description = "Invalid phase or missing fields"),
            @ApiResponse(responseCode = "404", description = "Room or match not found")
    })
    public ResponseEntity<Void> submitGuess(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code,
            @Valid @RequestBody GuessRequest request) {

        Guess guess = Guess.builder()
                .playerId(request.getPlayerId())
                .phase(GuessPhase.valueOf(request.getPhase()))
                .textAnswer(request.getTextAnswer())
                .guessX(request.getGuessX())
                .guessY(request.getGuessY())
                .guessZ(request.getGuessZ())
                .submittedAt(Instant.now())
                .build();

        gameUseCase.submitGuess(code, guess);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/results")
    @Operation(summary = "Get match results", responses = {
            @ApiResponse(responseCode = "200", description = "Results returned"),
            @ApiResponse(responseCode = "404", description = "Room or match not found")
    })
    public ResponseEntity<ResultsResponse> getResults(
            @Parameter(description = "Room code", example = "ABC123") @PathVariable String code) {

        List<Round> rounds = gameUseCase.getResults(code);
        ResultsResponse response = ResultsResponse.builder()
                .roomCode(code)
                .matchStatus("FINISHED")
                .rounds(rounds.stream().map(this::toRoundResult).toList())
                .build();

        return ResponseEntity.ok(response);
    }

    private RoundResponse toRoundResponse(Round round) {
        return RoundResponse.builder()
                .roundNumber(round.getRoundNumber())
                .gameId(round.getGamePackEntry().getGameId())
                .levelId(round.getGamePackEntry().getLevelId())
                .noclipHash(round.getGamePackEntry().getNoclipHash())
                .currentPhase(round.getCurrentPhase().name())
                .finished(round.isFinished())
                .startedAt(round.getStartedAt())
                .build();
    }

    private ResultsResponse.RoundResult toRoundResult(Round round) {
        return ResultsResponse.RoundResult.builder()
                .roundNumber(round.getRoundNumber())
                .gameId(round.getGamePackEntry().getGameId())
                .levelId(round.getGamePackEntry().getLevelId())
                .noclipHash(round.getGamePackEntry().getNoclipHash())
                .trueSpawnX(round.getGamePackEntry().getSpawnX())
                .trueSpawnZ(round.getGamePackEntry().getSpawnZ())
                .finished(round.isFinished())
                .build();
    }
}
