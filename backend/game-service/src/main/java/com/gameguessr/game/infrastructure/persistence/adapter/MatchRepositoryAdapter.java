package com.gameguessr.game.infrastructure.persistence.adapter;

import com.gameguessr.game.domain.model.GamePackEntry;
import com.gameguessr.game.domain.model.GuessPhase;
import com.gameguessr.game.domain.model.Match;
import com.gameguessr.game.domain.model.MatchStatus;
import com.gameguessr.game.domain.model.Round;
import com.gameguessr.game.domain.port.outbound.MatchRepository;
import com.gameguessr.game.infrastructure.persistence.entity.MatchEntity;
import com.gameguessr.game.infrastructure.persistence.entity.RoundEntity;
import com.gameguessr.game.infrastructure.persistence.repository.MatchJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MatchRepositoryAdapter implements MatchRepository {

    private final MatchJpaRepository jpaRepository;

    @Override
    public Match save(Match match) {
        MatchEntity entity = toEntity(match);
        MatchEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Match> findByRoomCode(String roomCode) {
        return jpaRepository.findByRoomCode(roomCode).map(this::toDomain);
    }

    @Override
    public boolean existsByRoomCode(String roomCode) {
        return jpaRepository.existsByRoomCode(roomCode);
    }

    private MatchEntity toEntity(Match match) {
        MatchEntity entity = MatchEntity.builder()
                .id(match.getId())
                .roomCode(match.getRoomCode())
                .hostId(match.getHostId())
                .gamePack(match.getGamePack())
                .status(match.getStatus().name())
                .playerIdsCsv(match.getPlayerIds() != null ? String.join(",", match.getPlayerIds()) : "")
                .currentRoundIndex(match.getCurrentRoundIndex())
                .build();

        List<RoundEntity> roundEntities = match.getRounds().stream()
                .map(r -> roundToEntity(r, entity))
                .toList();

        entity.setRounds(roundEntities);
        return entity;
    }

    private RoundEntity roundToEntity(Round round, MatchEntity matchEntity) {
        return RoundEntity.builder()
                .id(round.getId())
                .match(matchEntity)
                .roundNumber(round.getRoundNumber())
                .gameId(round.getGamePackEntry().getGameId())
                .levelId(round.getGamePackEntry().getLevelId())
                .noclipHash(round.getGamePackEntry().getNoclipHash())
                .spawnX(round.getGamePackEntry().getSpawnX())
                .spawnZ(round.getGamePackEntry().getSpawnZ())
                .currentPhase(round.getCurrentPhase().name())
                .finished(round.isFinished())
                .startedAt(round.getStartedAt())
                .guessedPlayerIdsCsv(round.getPhaseGuessedPlayerIds() != null
                        ? String.join(",", round.getPhaseGuessedPlayerIds()) : "")
                .build();
    }

    private Match toDomain(MatchEntity entity) {
        List<Round> rounds = entity.getRounds().stream()
                .map(this::roundToDomain)
                .toList();

        List<String> playerIds = (entity.getPlayerIdsCsv() != null && !entity.getPlayerIdsCsv().isEmpty())
                ? Arrays.asList(entity.getPlayerIdsCsv().split(","))
                : List.of();

        return Match.builder()
                .id(entity.getId())
                .roomCode(entity.getRoomCode())
                .hostId(entity.getHostId())
                .playerIds(playerIds)
                .gamePack(entity.getGamePack())
                .status(MatchStatus.valueOf(entity.getStatus()))
                .currentRoundIndex(entity.getCurrentRoundIndex())
                .rounds(rounds)
                .build();
    }

    private Round roundToDomain(RoundEntity entity) {
        Set<String> guessedPlayerIds = (entity.getGuessedPlayerIdsCsv() != null && !entity.getGuessedPlayerIdsCsv().isEmpty())
                ? Arrays.stream(entity.getGuessedPlayerIdsCsv().split(",")).collect(Collectors.toSet())
                : new HashSet<>();

        return Round.builder()
                .id(entity.getId())
                .roundNumber(entity.getRoundNumber())
                .gamePackEntry(GamePackEntry.builder()
                        .gameId(entity.getGameId())
                        .levelId(entity.getLevelId())
                        .noclipHash(entity.getNoclipHash())
                        .spawnX(entity.getSpawnX())
                        .spawnZ(entity.getSpawnZ())
                        .build())
                .currentPhase(GuessPhase.valueOf(entity.getCurrentPhase()))
                .finished(entity.isFinished())
                .startedAt(entity.getStartedAt())
                .phaseGuessedPlayerIds(guessedPlayerIds)
                .build();
    }
}
