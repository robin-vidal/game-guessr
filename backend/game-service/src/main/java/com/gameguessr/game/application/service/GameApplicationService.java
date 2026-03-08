package com.gameguessr.game.application.service;

import com.gameguessr.game.domain.exception.InvalidPhaseException;
import com.gameguessr.game.domain.exception.MatchAlreadyStartedException;
import com.gameguessr.game.domain.exception.MatchNotFoundException;
import com.gameguessr.game.domain.model.GamePackEntry;
import com.gameguessr.game.domain.model.Guess;
import com.gameguessr.game.domain.model.GuessPhase;
import com.gameguessr.game.domain.model.Match;
import com.gameguessr.game.domain.model.MatchStatus;
import com.gameguessr.game.domain.model.Round;
import com.gameguessr.game.domain.port.inbound.GameUseCase;
import com.gameguessr.game.domain.port.outbound.GameEventPublisher;
import com.gameguessr.game.domain.port.outbound.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing all game use cases.
 * Orchestrates domain logic and delegates to outbound ports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameApplicationService implements GameUseCase {

    private static final int ROUNDS_PER_MATCH = 5;

    private final MatchRepository matchRepository;
    private final GameEventPublisher gameEventPublisher;

    @Override
    @Transactional
    public Match startMatch(String roomCode, String hostId) {
        log.info("Starting match for room {}", roomCode);

        if (matchRepository.existsByRoomCode(roomCode)) {
            Match existing = matchRepository.findByRoomCode(roomCode).orElseThrow();
            if (existing.getStatus() != MatchStatus.WAITING) {
                throw new MatchAlreadyStartedException(roomCode);
            }
        }

        List<Round> rounds = buildRounds();

        Match match = Match.builder()
                .id(UUID.randomUUID())
                .roomCode(roomCode)
                .hostId(hostId)
                .status(MatchStatus.IN_PROGRESS)
                .rounds(rounds)
                .currentRoundIndex(0)
                .build();

        Match saved = matchRepository.save(match);

        // Notify WebSocket gateway about round 1 starting
        gameEventPublisher.publishRoundUpdate(roomCode, saved.currentRound());

        log.info("Match {} started for room {} with {} rounds", saved.getId(), roomCode, ROUNDS_PER_MATCH);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Round getCurrentRound(String roomCode) {
        Match match = findActiveMatch(roomCode);
        return match.currentRound();
    }

    @Override
    @Transactional
    public void submitGuess(String roomCode, Guess guess) {
        Match match = findActiveMatch(roomCode);
        Round currentRound = match.currentRound();

        validatePhase(currentRound, guess);

        log.info("Player {} submitted {} guess for room {} round {}",
                guess.getPlayerId(), guess.getPhase(), roomCode, currentRound.getRoundNumber());

        // Publish to Kafka — Scoring Service consumes this
        gameEventPublisher.publishGuessSubmitted(roomCode, currentRound.getRoundNumber(), guess);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Round> getResults(String roomCode) {
        Match match = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new MatchNotFoundException(roomCode));
        return List.copyOf(match.getRounds());
    }

    // ── Private helpers ──────────────────────────────────────────────

    private Match findActiveMatch(String roomCode) {
        Match match = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new MatchNotFoundException(roomCode));

        if (!match.isInProgress()) {
            throw new IllegalStateException("Match for room " + roomCode + " is not in progress.");
        }
        return match;
    }

    /**
     * Builds 5 rounds with placeholder (0, 0, 0) coordinates.
     * TODO: wire up actual game pack data source (DB / external API) here.
     */
    private List<Round> buildRounds() {
        List<Round> rounds = new ArrayList<>();
        for (int i = 1; i <= ROUNDS_PER_MATCH; i++) {
            rounds.add(Round.builder()
                    .id(UUID.randomUUID())
                    .roundNumber(i)
                    .gamePackEntry(GamePackEntry.builder()
                            .gameId("mario-kart-8")
                            .levelId("" + i)
                            // spawnX / spawnY / spawnZ default to 0.0 via @Builder.Default
                            .build())
                    .currentPhase(GuessPhase.GAME)
                    .finished(false)
                    .startedAt(Instant.now().toEpochMilli())
                    .build());
        }
        return rounds;
    }

    private void validatePhase(Round round, Guess guess) {
        GuessPhase current = round.getCurrentPhase();

        if (guess.getPhase() == GuessPhase.LEVEL && current == GuessPhase.GAME) {
            throw new InvalidPhaseException(
                    "Cannot submit LEVEL guess — GAME phase not yet completed.");
        }
        if (guess.getPhase() == GuessPhase.SPOT && current != GuessPhase.SPOT) {
            throw new InvalidPhaseException(
                    "Cannot submit SPOT guess — previous phases not yet completed.");
        }
    }
}
