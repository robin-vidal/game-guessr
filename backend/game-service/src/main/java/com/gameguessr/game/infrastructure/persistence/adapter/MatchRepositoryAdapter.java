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

import java.util.List;
import java.util.Optional;

/**
 * Driven adapter — implements the domain MatchRepository port using JPA.
 */
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

    // ── Mapping ──────────────────────────────────────────────────────

    private MatchEntity toEntity(Match match) {
        MatchEntity entity = MatchEntity.builder()
                .id(match.getId())
                .roomCode(match.getRoomCode())
                .hostId(match.getHostId())
                .status(match.getStatus().name())
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
                .spawnX(round.getGamePackEntry().getSpawnX())
                .spawnY(round.getGamePackEntry().getSpawnY())
                .spawnZ(round.getGamePackEntry().getSpawnZ())
                .currentPhase(round.getCurrentPhase().name())
                .finished(round.isFinished())
                .startedAt(round.getStartedAt())
                .build();
    }

    private Match toDomain(MatchEntity entity) {
        List<Round> rounds = entity.getRounds().stream()
                .map(this::roundToDomain)
                .toList();

        return Match.builder()
                .id(entity.getId())
                .roomCode(entity.getRoomCode())
                .hostId(entity.getHostId())
                .status(MatchStatus.valueOf(entity.getStatus()))
                .currentRoundIndex(entity.getCurrentRoundIndex())
                .rounds(rounds)
                .build();
    }

    private Round roundToDomain(RoundEntity entity) {
        return Round.builder()
                .id(entity.getId())
                .roundNumber(entity.getRoundNumber())
                .gamePackEntry(GamePackEntry.builder()
                        .gameId(entity.getGameId())
                        .levelId(entity.getLevelId())
                        .spawnX(entity.getSpawnX())
                        .spawnY(entity.getSpawnY())
                        .spawnZ(entity.getSpawnZ())
                        .build())
                .currentPhase(GuessPhase.valueOf(entity.getCurrentPhase()))
                .finished(entity.isFinished())
                .startedAt(entity.getStartedAt())
                .build();
    }
}
