package com.gameguessr.leaderboard.domain.port.outbound;

import com.gameguessr.leaderboard.domain.model.LeaderboardEntry;

import java.util.List;

/**
 * Outbound port — persistence contract for leaderboard operations.
 * Backed by Redis Sorted Sets (ZSET).
 */
public interface LeaderboardRepository {

    /**
     * Increment a player's score in the specified leaderboard key.
     *
     * @param key      the Redis ZSET key (e.g. "leaderboard:global")
     * @param playerId the player member
     * @param delta    the score increment
     */
    void incrementScore(String key, String playerId, double delta);

    /**
     * Get top-N entries from the specified leaderboard key (highest first).
     *
     * @param key  the Redis ZSET key
     * @param topN number of entries
     * @return ordered list of entries (rank 1 = highest score)
     */
    List<LeaderboardEntry> getTopN(String key, int topN);

    /**
     * Get a player's score from the specified leaderboard key.
     *
     * @param key      the Redis ZSET key
     * @param playerId the player member
     * @return the score, or null if not present
     */
    Double getScore(String key, String playerId);

    /**
     * Get a player's rank in the specified leaderboard key (0-based, highest
     * first).
     *
     * @param key      the Redis ZSET key
     * @param playerId the player member
     * @return the rank (0-based), or null if not present
     */
    Long getRank(String key, String playerId);
}
