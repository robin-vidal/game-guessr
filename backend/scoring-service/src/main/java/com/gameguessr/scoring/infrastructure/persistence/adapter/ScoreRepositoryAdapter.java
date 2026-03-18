package com.gameguessr.scoring.infrastructure.persistence.adapter;

import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.domain.port.outbound.ScoreRepository;
import com.gameguessr.scoring.infrastructure.persistence.entity.ScoreEntity;
import com.gameguessr.scoring.infrastructure.persistence.repository.ScoreJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScoreRepositoryAdapter implements ScoreRepository {

    private final ScoreJpaRepository jpaRepository;

    @Override
    public Score save(Score score) {
        ScoreEntity entity = toEntity(score);
        ScoreEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Score> findByRoomCodeAndRoundNumber(String roomCode, int roundNumber) {
        return jpaRepository.findByRoomCodeAndRoundNumber(roomCode, roundNumber)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Score> findByRoomCode(String roomCode) {
        return jpaRepository.findByRoomCode(roomCode)
                .stream().map(this::toDomain).toList();
    }

    private ScoreEntity toEntity(Score score) {
        return ScoreEntity.builder()
                .id(score.getId())
                .roomCode(score.getRoomCode())
                .roundNumber(score.getRoundNumber())
                .playerId(score.getPlayerId())
                .phase(score.getPhase())
                .points(score.getPoints())
                .correct(score.isCorrect())
                .timeBonusMs(score.getTimeBonusMs())
                .distanceError(score.getDistanceError())
                .createdAt(score.getCreatedAt())
                .build();
    }

    private Score toDomain(ScoreEntity entity) {
        return Score.builder()
                .id(entity.getId())
                .roomCode(entity.getRoomCode())
                .roundNumber(entity.getRoundNumber())
                .playerId(entity.getPlayerId())
                .phase(entity.getPhase())
                .points(entity.getPoints())
                .correct(entity.isCorrect())
                .timeBonusMs(entity.getTimeBonusMs())
                .distanceError(entity.getDistanceError())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
