package com.gameguessr.scoring.domain.port.outbound;

import com.gameguessr.scoring.domain.model.Score;

/**
 * Outbound port — Kafka event publishing for scoring results.
 */
public interface ScoringEventPublisher {

    /**
     * Publishes a {@code score.calculated} event.
     * Consumed by the Leaderboard Service.
     */
    void publishScoreCalculated(Score score);
}
