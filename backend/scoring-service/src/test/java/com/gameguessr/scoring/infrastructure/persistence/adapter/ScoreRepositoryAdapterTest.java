package com.gameguessr.scoring.infrastructure.persistence.adapter;

import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.infrastructure.persistence.entity.ScoreEntity;
import com.gameguessr.scoring.infrastructure.persistence.repository.ScoreJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoreRepositoryAdapter")
class ScoreRepositoryAdapterTest {

    @Mock
    private ScoreJpaRepository jpaRepository;

    @InjectMocks
    private ScoreRepositoryAdapter adapter;

    private static final String ROOM_CODE = "ABC123";
    private static final String PLAYER_ID = "player-1";

    @Test
    @DisplayName("save — maps domain to entity and returns domain")
    void save_mapsDomainToEntityAndBack() {
        Score score = buildScore();
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = adapter.save(score);

        assertThat(result.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(result.getPlayerId()).isEqualTo(PLAYER_ID);
        assertThat(result.getPhase()).isEqualTo("GAME");
        assertThat(result.getPoints()).isEqualTo(1000);
        assertThat(result.isCorrect()).isTrue();
        verify(jpaRepository).save(any(ScoreEntity.class));
    }

    @Test
    @DisplayName("findByRoomCodeAndRoundNumber — returns mapped domain list")
    void findByRoomCodeAndRoundNumber_returnsMappedList() {
        ScoreEntity entity = buildEntity();
        when(jpaRepository.findByRoomCodeAndRoundNumber(ROOM_CODE, 1))
                .thenReturn(List.of(entity));

        List<Score> result = adapter.findByRoomCodeAndRoundNumber(ROOM_CODE, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(result.get(0).getPoints()).isEqualTo(1000);
    }

    @Test
    @DisplayName("findByRoomCode — returns all scores for room")
    void findByRoomCode_returnsAllScores() {
        when(jpaRepository.findByRoomCode(ROOM_CODE)).thenReturn(List.of(buildEntity()));

        List<Score> result = adapter.findByRoomCode(ROOM_CODE);

        assertThat(result).hasSize(1);
        verify(jpaRepository).findByRoomCode(ROOM_CODE);
    }

    @Test
    @DisplayName("save — round trip preserves timeBonusMs and distanceError")
    void save_roundTripPreservesExtraFields() {
        Score score = Score.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .roundNumber(1)
                .playerId(PLAYER_ID)
                .phase("LEVEL")
                .points(650)
                .correct(true)
                .timeBonusMs(150L)
                .distanceError(0.0)
                .createdAt(Instant.now())
                .build();
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = adapter.save(score);

        assertThat(result.getTimeBonusMs()).isEqualTo(150L);
        assertThat(result.getPoints()).isEqualTo(650);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Score buildScore() {
        return Score.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .roundNumber(1)
                .playerId(PLAYER_ID)
                .phase("GAME")
                .points(1000)
                .correct(true)
                .timeBonusMs(0L)
                .distanceError(0.0)
                .createdAt(Instant.now())
                .build();
    }

    private ScoreEntity buildEntity() {
        return ScoreEntity.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .roundNumber(1)
                .playerId(PLAYER_ID)
                .phase("GAME")
                .points(1000)
                .correct(true)
                .timeBonusMs(0L)
                .distanceError(0.0)
                .createdAt(Instant.now())
                .build();
    }
}
