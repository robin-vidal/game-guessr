package com.gameguessr.scoring.domain.port.outbound;

import com.gameguessr.scoring.domain.model.Score;

import java.util.List;

/**
 * Outbound port — persistence contract for Score aggregates.
 */
public interface ScoreRepository {

    Score save(Score score);

    List<Score> findByRoomCodeAndRoundNumber(String roomCode, int roundNumber);

    List<Score> findByRoomCode(String roomCode);
}
