package com.gameguessr.leaderboard.application.service;

import com.gameguessr.leaderboard.domain.model.LeaderboardEntry;
import com.gameguessr.leaderboard.domain.port.inbound.LeaderboardUseCase;
import com.gameguessr.leaderboard.domain.port.outbound.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service implementing leaderboard use cases.
 * Uses Redis Sorted Sets (ZSET) for real-time rankings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardApplicationService implements LeaderboardUseCase {

    private final LeaderboardRepository leaderboardRepository;

    @Value("${leaderboard.global-key:leaderboard:global}")
    private String globalKey;

    @Value("${leaderboard.room-key-prefix:leaderboard:room:}")
    private String roomKeyPrefix;

    @Value("${leaderboard.default-top-n:100}")
    private int defaultTopN;

    @Override
    public void recordScore(String roomCode, String playerId, int points) {
        log.info("Recording {} pts for player {} in room {}", points, playerId, roomCode);

        // Update global leaderboard
        leaderboardRepository.incrementScore(globalKey, playerId, points);

        // Update room-specific leaderboard
        String roomKey = roomKeyPrefix + roomCode;
        leaderboardRepository.incrementScore(roomKey, playerId, points);

        log.debug("Updated leaderboards for player {} (+{} pts)", playerId, points);
    }

    @Override
    public List<LeaderboardEntry> getGlobalLeaderboard(int topN) {
        int limit = topN > 0 ? topN : defaultTopN;
        return leaderboardRepository.getTopN(globalKey, limit);
    }

    @Override
    public List<LeaderboardEntry> getRoomLeaderboard(String roomCode) {
        String roomKey = roomKeyPrefix + roomCode;
        return leaderboardRepository.getTopN(roomKey, defaultTopN);
    }
}
