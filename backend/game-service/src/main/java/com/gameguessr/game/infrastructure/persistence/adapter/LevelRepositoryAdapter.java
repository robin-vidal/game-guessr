package com.gameguessr.game.infrastructure.persistence.adapter;

import com.gameguessr.game.domain.model.Level;
import com.gameguessr.game.domain.model.LevelCoordinate;
import com.gameguessr.game.domain.port.outbound.LevelRepository;
import com.gameguessr.game.infrastructure.persistence.entity.LevelCoordinateEntity;
import com.gameguessr.game.infrastructure.persistence.entity.LevelEntity;
import com.gameguessr.game.infrastructure.persistence.repository.JpaLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LevelRepositoryAdapter implements LevelRepository {

    private final JpaLevelRepository jpaRepository;

    @Override
    public List<Level> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Level save(Level level) {
        LevelEntity entity = toEntity(level);
        LevelEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<String> findGamePacks() {
        return jpaRepository.findDistinctGamePacks();
    }

    @Override
    public List<String> findLevelNames(String query) {
        return jpaRepository
                .findByLevelNameContainingIgnoreCase(query, PageRequest.of(0, 8))
                .stream()
                .map(LevelEntity::getLevelName)
                .toList();
    }

    private LevelEntity toEntity(Level level) {
        LevelEntity entity = LevelEntity.builder()
                .id(level.getId())
                .gamePack(level.getGamePack())
                .levelName(level.getLevelName())
                .build();

        List<LevelCoordinateEntity> coordEntities = level.getCoordinates().stream()
                .map(c -> toCoordinateEntity(c, entity))
                .toList();

        entity.setCoordinates(coordEntities);
        return entity;
    }

    private LevelCoordinateEntity toCoordinateEntity(LevelCoordinate coord, LevelEntity levelEntity) {
        return LevelCoordinateEntity.builder()
                .id(coord.getId())
                .level(levelEntity)
                .noclipHash(coord.getNoclipHash())
                .spawnX(coord.getSpawnX())
                .spawnZ(coord.getSpawnZ())
                .build();
    }

    private Level toDomain(LevelEntity entity) {
        List<LevelCoordinate> coordinates = entity.getCoordinates().stream()
                .map(this::toCoordinateDomain)
                .toList();

        return Level.builder()
                .id(entity.getId())
                .gamePack(entity.getGamePack())
                .levelName(entity.getLevelName())
                .coordinates(coordinates)
                .build();
    }

    private LevelCoordinate toCoordinateDomain(LevelCoordinateEntity entity) {
        return LevelCoordinate.builder()
                .id(entity.getId())
                .noclipHash(entity.getNoclipHash())
                .spawnX(entity.getSpawnX())
                .spawnZ(entity.getSpawnZ())
                .build();
    }
}
