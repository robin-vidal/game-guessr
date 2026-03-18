package com.gameguessr.scoring.infrastructure.persistence.repository;

import com.gameguessr.scoring.infrastructure.persistence.entity.ScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScoreJpaRepository extends JpaRepository<ScoreEntity, UUID> {

    List<ScoreEntity> findByRoomCodeAndRoundNumber(String roomCode, int roundNumber);

    List<ScoreEntity> findByRoomCode(String roomCode);
}
