package com.gameguessr.game.infrastructure.persistence.repository;

import com.gameguessr.game.infrastructure.persistence.entity.LevelEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaLevelRepository extends JpaRepository<LevelEntity, UUID> {

    @Query("SELECT DISTINCT e.gamePack FROM LevelEntity e")
    List<String> findDistinctGamePacks();

    List<LevelEntity> findByLevelNameContainingIgnoreCase(String query, Pageable pageable);
}
