package com.gameguessr.leaderboard.infrastructure.persistence.adapter;

import com.gameguessr.leaderboard.domain.model.LeaderboardEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisLeaderboardRepositoryAdapter")
class RedisLeaderboardRepositoryAdapterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOps;

    @InjectMocks
    private RedisLeaderboardRepositoryAdapter adapter;

    private static final String KEY = "leaderboard:global";
    private static final String PLAYER_ID = "player-1";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    @DisplayName("incrementScore — delegates to opsForZSet().incrementScore")
    void incrementScore_delegates() {
        adapter.incrementScore(KEY, PLAYER_ID, 1000.0);

        verify(zSetOps).incrementScore(KEY, PLAYER_ID, 1000.0);
    }

    @Test
    @DisplayName("getTopN — returns entries in descending order with ranks")
    void getTopN_returnsEntriesWithRanks() {
        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(buildTuple("player-1", 3000.0));
        tuples.add(buildTuple("player-2", 2000.0));

        when(zSetOps.reverseRangeWithScores(KEY, 0, 1)).thenReturn(tuples);

        List<LeaderboardEntry> result = adapter.getTopN(KEY, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlayerId()).isEqualTo("player-1");
        assertThat(result.get(0).getScore()).isEqualTo(3000.0);
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(1).getPlayerId()).isEqualTo("player-2");
        assertThat(result.get(1).getRank()).isEqualTo(2);
    }

    @Test
    @DisplayName("getTopN — returns empty list when tuples is null")
    void getTopN_nullTuples_returnsEmptyList() {
        when(zSetOps.reverseRangeWithScores(KEY, 0, 4)).thenReturn(null);

        List<LeaderboardEntry> result = adapter.getTopN(KEY, 5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getTopN — returns empty list when tuples is empty")
    void getTopN_emptyTuples_returnsEmptyList() {
        when(zSetOps.reverseRangeWithScores(KEY, 0, 4)).thenReturn(Set.of());

        List<LeaderboardEntry> result = adapter.getTopN(KEY, 5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getTopN — handles null score in tuple (defaults to 0.0)")
    void getTopN_nullScore_defaultsToZero() {
        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(buildTuple("player-1", null));

        when(zSetOps.reverseRangeWithScores(KEY, 0, 0)).thenReturn(tuples);

        List<LeaderboardEntry> result = adapter.getTopN(KEY, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getScore — delegates to opsForZSet().score")
    void getScore_delegates() {
        when(zSetOps.score(KEY, PLAYER_ID)).thenReturn(1500.0);

        Double result = adapter.getScore(KEY, PLAYER_ID);

        assertThat(result).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("getRank — delegates to opsForZSet().reverseRank")
    void getRank_delegates() {
        when(zSetOps.reverseRank(KEY, PLAYER_ID)).thenReturn(0L);

        Long result = adapter.getRank(KEY, PLAYER_ID);

        assertThat(result).isEqualTo(0L);
    }

    // ── helpers ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private ZSetOperations.TypedTuple<Object> buildTuple(String playerId, Double score) {
        ZSetOperations.TypedTuple<Object> tuple = mock(ZSetOperations.TypedTuple.class);
        when(tuple.getValue()).thenReturn(playerId);
        when(tuple.getScore()).thenReturn(score);
        return tuple;
    }
}
