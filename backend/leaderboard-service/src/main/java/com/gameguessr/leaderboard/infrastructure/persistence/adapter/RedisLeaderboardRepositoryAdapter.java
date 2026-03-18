package com.gameguessr.leaderboard.infrastructure.persistence.adapter;

import com.gameguessr.leaderboard.domain.model.LeaderboardEntry;
import com.gameguessr.leaderboard.domain.port.outbound.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Driven adapter — implements LeaderboardRepository using Redis Sorted Sets
 * (ZSET).
 *
 * <p>
 * Uses {@code ZINCRBY} for atomic score increments and {@code ZREVRANGE} for
 * descending rank retrieval (highest score = rank 1).
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLeaderboardRepositoryAdapter implements LeaderboardRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void incrementScore(String key, String playerId, double delta) {
        redisTemplate.opsForZSet().incrementScore(key, playerId, delta);
    }

    @Override
    public List<LeaderboardEntry> getTopN(String key, int topN) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0,
                topN - 1);

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<LeaderboardEntry> entries = new ArrayList<>();
        long rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            entries.add(LeaderboardEntry.builder()
                    .playerId(tuple.getValue().toString())
                    .score(tuple.getScore() != null ? tuple.getScore() : 0.0)
                    .rank(rank++)
                    .build());
        }
        return entries;
    }

    @Override
    public Double getScore(String key, String playerId) {
        return redisTemplate.opsForZSet().score(key, playerId);
    }

    @Override
    public Long getRank(String key, String playerId) {
        return redisTemplate.opsForZSet().reverseRank(key, playerId);
    }
}
