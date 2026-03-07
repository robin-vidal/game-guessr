package com.gameguessr.leaderboard.domain.port.inbound;

import com.gameguessr.leaderboard.domain.model.LeaderboardEntry;

import java.util.List;

/**
 * Inbound port — defines all use cases for the Leaderboard Service.
 */
public interface LeaderboardUseCase {

    /**
     * Records a score for a player. Updates both global and room leaderboards.
     *
     * @param roomCode the room identifier
     * @param playerId the player's ID
     * @param points   the points to add
     */
    void recordScore(String roomCode, String playerId, int points);

    /**
     * Returns the global top-N leaderboard.
     *
     * @param topN number of entries to return
     * @return ordered list of {@link LeaderboardEntry}
     */
    List<LeaderboardEntry> getGlobalLeaderboard(int topN);

    /**
     * Returns the leaderboard for a specific room.
     *
     * @param roomCode the room identifier
     * @return ordered list of {@link LeaderboardEntry}
     */
    List<LeaderboardEntry> getRoomLeaderboard(String roomCode);
}
