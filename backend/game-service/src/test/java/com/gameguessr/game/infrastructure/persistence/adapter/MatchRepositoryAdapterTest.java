package com.gameguessr.game.infrastructure.persistence.adapter;

import com.gameguessr.game.domain.model.*;
import com.gameguessr.game.infrastructure.persistence.entity.MatchEntity;
import com.gameguessr.game.infrastructure.persistence.entity.RoundEntity;
import com.gameguessr.game.infrastructure.persistence.repository.MatchJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchRepositoryAdapter")
class MatchRepositoryAdapterTest {

    @Mock
    private MatchJpaRepository jpaRepository;

    @InjectMocks
    private MatchRepositoryAdapter adapter;

    private static final String ROOM_CODE = "ABC123";
    private static final String HOST_ID = "host-1";

    @Test
    @DisplayName("save — maps domain to entity and returns domain")
    void save_mapsDomainToEntityAndBack() {
        Match match = buildMatch();
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Match result = adapter.save(match);

        assertThat(result.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(result.getHostId()).isEqualTo(HOST_ID);
        assertThat(result.getGamePack()).isEqualTo("mario-kart-wii");
        assertThat(result.getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
        verify(jpaRepository).save(any(MatchEntity.class));
    }

    @Test
    @DisplayName("save — round trip preserves round fields including noclipHash")
    void save_roundTripPreservesFields() {
        Match match = buildMatch();
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Match result = adapter.save(match);

        assertThat(result.getRounds()).hasSize(1);
        Round round = result.getRounds().get(0);
        assertThat(round.getRoundNumber()).isEqualTo(1);
        assertThat(round.getCurrentPhase()).isEqualTo(GuessPhase.GAME);
        assertThat(round.getGamePackEntry().getNoclipHash()).isEqualTo("mkwii/beginner_course;ShareData=test");
        assertThat(round.isFinished()).isFalse();
    }

    @Test
    @DisplayName("findByRoomCode — returns mapped domain when found")
    void findByRoomCode_found_returnsDomain() {
        MatchEntity entity = buildEntity();
        when(jpaRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(entity));

        Optional<Match> result = adapter.findByRoomCode(ROOM_CODE);

        assertThat(result).isPresent();
        assertThat(result.get().getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(result.get().getGamePack()).isEqualTo("mario-kart-wii");
        assertThat(result.get().getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("findByRoomCode — returns empty when not found")
    void findByRoomCode_notFound_returnsEmpty() {
        when(jpaRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.empty());

        assertThat(adapter.findByRoomCode(ROOM_CODE)).isEmpty();
    }

    @Test
    @DisplayName("existsByRoomCode — delegates to jpaRepository")
    void existsByRoomCode_delegates() {
        when(jpaRepository.existsByRoomCode(ROOM_CODE)).thenReturn(true);

        assertThat(adapter.existsByRoomCode(ROOM_CODE)).isTrue();
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Match buildMatch() {
        Round round = Round.builder()
                .id(UUID.randomUUID())
                .roundNumber(1)
                .gamePackEntry(GamePackEntry.builder()
                        .gameId("mario-kart-wii")
                        .levelId("Luigi Circuit")
                        .noclipHash("mkwii/beginner_course;ShareData=test")
                        .spawnX(0.0)
                        .spawnZ(0.0)
                        .build())
                .currentPhase(GuessPhase.GAME)
                .finished(false)
                .startedAt(Instant.now().toEpochMilli())
                .build();

        return Match.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .gamePack("mario-kart-wii")
                .status(MatchStatus.IN_PROGRESS)
                .rounds(List.of(round))
                .currentRoundIndex(0)
                .build();
    }

    private MatchEntity buildEntity() {
        MatchEntity entity = MatchEntity.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .gamePack("mario-kart-wii")
                .status("IN_PROGRESS")
                .currentRoundIndex(0)
                .build();

        RoundEntity roundEntity = RoundEntity.builder()
                .id(UUID.randomUUID())
                .match(entity)
                .roundNumber(1)
                .gameId("mario-kart-wii")
                .levelId("Luigi Circuit")
                .noclipHash("mkwii/beginner_course;ShareData=test")
                .currentPhase("GAME")
                .finished(false)
                .startedAt(Instant.now().toEpochMilli())
                .build();

        entity.setRounds(List.of(roundEntity));
        return entity;
    }
}
