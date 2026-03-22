package com.gameguessr.game.infrastructure.persistence.repository;

import com.gameguessr.game.infrastructure.persistence.entity.LevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaLevelRepository extends JpaRepository<LevelEntity, UUID> {
}
