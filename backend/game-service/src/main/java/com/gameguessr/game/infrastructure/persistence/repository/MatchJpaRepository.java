package com.gameguessr.game.infrastructure.persistence.repository;

import com.gameguessr.game.infrastructure.persistence.entity.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository — infrastructure layer only.
 */
@Repository
public interface MatchJpaRepository extends JpaRepository<MatchEntity, UUID> {

    Optional<MatchEntity> findByRoomCode(String roomCode);

    boolean existsByRoomCode(String roomCode);
}
