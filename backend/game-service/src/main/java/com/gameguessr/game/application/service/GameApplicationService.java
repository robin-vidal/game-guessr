package com.gameguessr.game.application.service;

import com.gameguessr.game.domain.exception.DuplicateGuessException;
import com.gameguessr.game.domain.exception.InvalidPhaseException;
import com.gameguessr.game.domain.exception.MatchAlreadyStartedException;
import com.gameguessr.game.domain.exception.MatchNotFoundException;
import com.gameguessr.game.domain.model.GamePackEntry;
import com.gameguessr.game.domain.model.Guess;
import com.gameguessr.game.domain.model.GuessPhase;
import com.gameguessr.game.domain.model.Level;
import com.gameguessr.game.domain.model.LevelCoordinate;
import com.gameguessr.game.domain.model.Match;
import com.gameguessr.game.domain.model.MatchStatus;
import com.gameguessr.game.domain.model.Round;
import com.gameguessr.game.domain.port.inbound.GameUseCase;
import com.gameguessr.game.domain.port.outbound.GameEventPublisher;
import com.gameguessr.game.domain.port.outbound.LevelRepository;
import com.gameguessr.game.domain.port.outbound.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameApplicationService implements GameUseCase {

    private static final int ROUNDS_PER_MATCH = 5;

    private final MatchRepository matchRepository;
    private final LevelRepository levelRepository;
    private final GameEventPublisher gameEventPublisher;

    @Override
    @Transactional
    public Match startMatch(String roomCode, String hostId, List<String> playerIds) {
        log.info("Starting match for room {}", roomCode);

        Match existing = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new MatchNotFoundException(roomCode));

        if (existing.getStatus() != MatchStatus.WAITING) {
            throw new MatchAlreadyStartedException(roomCode);
        }

        List<Round> rounds = buildRoundsFromLevels();

        Match match = existing.withStatus(MatchStatus.IN_PROGRESS)
                .withPlayerIds(playerIds)
                .withRounds(rounds)
                .withCurrentRoundIndex(0);

        Match saved = matchRepository.save(match);

        gameEventPublisher.publishRoundUpdate(roomCode, saved.currentRound());

        log.info("Match {} started for room {} with {} rounds and {} players",
                saved.getId(), roomCode, rounds.size(), playerIds.size());
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

        // ── Duplicate guard ──────────────────────────────────────────────
        if (currentRound.getPhaseGuessedPlayerIds().contains(guess.getPlayerId())) {
            throw new DuplicateGuessException(
                    "Player " + guess.getPlayerId() + " already guessed for phase " + guess.getPhase());
        }

        log.info("Player {} submitted {} guess for room {} round {}",
                guess.getPlayerId(), guess.getPhase(), roomCode, currentRound.getRoundNumber());

        gameEventPublisher.publishGuessSubmitted(roomCode, currentRound.getRoundNumber(), guess);

        // ── Record guess ─────────────────────────────────────────────────
        Set<String> updatedGuessedIds = new HashSet<>(currentRound.getPhaseGuessedPlayerIds());
        updatedGuessedIds.add(guess.getPlayerId());
        Round updatedRound = currentRound.withPhaseGuessedPlayerIds(updatedGuessedIds);

        // ── Phase & round progression (only when all players have guessed) ──
        boolean allGuessed = updatedGuessedIds.size() >= match.getPlayerIds().size();

        if (allGuessed) {
            updatedRound = advancePhase(updatedRound, guess.getPhase());
            updatedRound = updatedRound.withPhaseGuessedPlayerIds(new HashSet<>());
        }

        match = replaceCurrentRound(match, updatedRound);

        if (allGuessed && updatedRound.isFinished()) {
            if (match.getCurrentRoundIndex() < match.getRounds().size() - 1) {
                match = match.withCurrentRoundIndex(match.getCurrentRoundIndex() + 1);
            } else {
                match = match.withStatus(MatchStatus.FINISHED);
            }
        }

        matchRepository.save(match);
        gameEventPublisher.publishRoundUpdate(roomCode, match.currentRound());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Round> getResults(String roomCode) {
        Match match = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new MatchNotFoundException(roomCode));
        return List.copyOf(match.getRounds());
    }

    private Match findActiveMatch(String roomCode) {
        Match match = matchRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new MatchNotFoundException(roomCode));

        if (!match.isInProgress()) {
            throw new IllegalStateException("Match for room " + roomCode + " is not in progress.");
        }
        return match;
    }

    private List<Round> buildRoundsFromLevels() {
        List<Level> allLevels = new ArrayList<>(levelRepository.findAll());
        if (allLevels.isEmpty()) {
            throw new IllegalStateException("No levels available");
        }

        Collections.shuffle(allLevels);

        int count = Math.min(ROUNDS_PER_MATCH, allLevels.size());
        List<Round> rounds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Level level = allLevels.get(i);
            List<LevelCoordinate> coords = level.getCoordinates();
            LevelCoordinate coord = coords.get(ThreadLocalRandom.current().nextInt(coords.size()));

            rounds.add(Round.builder()
                    .id(UUID.randomUUID())
                    .roundNumber(i + 1)
                    .gamePackEntry(GamePackEntry.builder()
                            .gameId(level.getGamePack())
                            .levelId(level.getLevelName())
                            .noclipHash(coord.getNoclipHash())
                            .spawnX(coord.getSpawnX())
                            .spawnZ(coord.getSpawnZ())
                            .build())
                    .currentPhase(GuessPhase.GAME)
                    .finished(false)
                    .startedAt(Instant.now().toEpochMilli())
                    .build());
        }

        return rounds;
    }

    private Round advancePhase(Round round, GuessPhase guessPhase) {
        return switch (guessPhase) {
            case GAME -> round.withCurrentPhase(GuessPhase.LEVEL);
            case LEVEL -> round.withCurrentPhase(GuessPhase.SPOT);
            case SPOT -> round.withFinished(true);
        };
    }

    private Match replaceCurrentRound(Match match, Round updatedRound) {
        List<Round> updatedRounds = new ArrayList<>(match.getRounds());
        updatedRounds.set(match.getCurrentRoundIndex(), updatedRound);
        return match.withRounds(updatedRounds);
    }

    private void validatePhase(Round round, Guess guess) {
        GuessPhase current = round.getCurrentPhase();

        if (guess.getPhase() != current) {
            throw new InvalidPhaseException(
                    "Cannot submit " + guess.getPhase() + " guess — current phase is " + current + ".");
        }
    }
}
