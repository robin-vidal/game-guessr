package com.gameguessr.scoring.application.service;

import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.domain.port.inbound.ScoringUseCase;
import com.gameguessr.scoring.domain.port.outbound.ScoreRepository;
import com.gameguessr.scoring.domain.port.outbound.ScoringEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing scoring use cases.
 *
 * <p>
 * Scoring logic (from design doc):
 * <ul>
 * <li>Phase 1 (GAME): Binary — correct = 1000 pts, incorrect = 0</li>
 * <li>Phase 2 (LEVEL): Base 500 pts + time bonus (up to 500 pts)</li>
 * <li>Phase 3 (SPOT): Post-MVP — Euclidean distance (0–5000 pts)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringApplicationService implements ScoringUseCase {

    private final ScoreRepository scoreRepository;
    private final ScoringEventPublisher scoringEventPublisher;

    @Value("${scoring.points.game-correct:1000}")
    private int gameCorrectPoints;

    @Value("${scoring.points.level-base:500}")
    private int levelBasePoints;

    @Value("${scoring.points.level-time-bonus-max:500}")
    private int levelTimeBonusMax;

    @Value("${scoring.points.spot-max:5000}")
    private int spotMaxPoints;

    @Override
    @Transactional
    public Score scoreGuess(String roomCode, int roundNumber, String playerId,
            String phase, String textAnswer,
            Double guessX, Double guessY, Double guessZ,
            String submittedAt) {

        log.info("Scoring {} guess for player {} in room {} round {}",
                phase, playerId, roomCode, roundNumber);

        int points;
        boolean isCorrect;
        long timeBonusMs = 0;
        double distanceError = 0.0;

        switch (phase) {
            case "GAME" -> {
                // MVP: always correct for demo purposes
                // TODO: wire up actual game title validation
                isCorrect = textAnswer != null && !textAnswer.isBlank();
                points = isCorrect ? gameCorrectPoints : 0;
            }
            case "LEVEL" -> {
                // MVP: always correct for demo purposes
                // TODO: wire up actual level validation
                isCorrect = textAnswer != null && !textAnswer.isBlank();

                // Calculate time bonus: faster answers get more bonus points
                if (isCorrect && submittedAt != null) {
                    try {
                        Instant submitted = Instant.parse(submittedAt);
                        long secondsElapsed = Duration.between(submitted, Instant.now()).abs().getSeconds();
                        // Max bonus for answers within 5 seconds, scales down to 0 over 60 seconds
                        double bonusRatio = Math.max(0, 1.0 - (secondsElapsed / 60.0));
                        timeBonusMs = (long) (bonusRatio * levelTimeBonusMax);
                    } catch (Exception e) {
                        log.warn("Failed to parse submittedAt for time bonus: {}", submittedAt);
                        timeBonusMs = 0;
                    }
                }

                points = isCorrect ? (int) (levelBasePoints + timeBonusMs) : 0;
            }
            case "SPOT" -> {
                // Post-MVP: stub with placeholder scoring
                isCorrect = false;
                distanceError = 0.0;
                points = 0;
                log.info("SPOT phase scoring is post-MVP — returning 0 points");
            }
            default -> {
                log.warn("Unknown phase: {} — scoring 0 points", phase);
                isCorrect = false;
                points = 0;
            }
        }

        Score score = Score.builder()
                .id(UUID.randomUUID())
                .roomCode(roomCode)
                .roundNumber(roundNumber)
                .playerId(playerId)
                .phase(phase)
                .points(points)
                .correct(isCorrect)
                .timeBonusMs(timeBonusMs)
                .distanceError(distanceError)
                .createdAt(Instant.now())
                .build();

        Score saved = scoreRepository.save(score);

        // Publish score.calculated → consumed by Leaderboard Service
        scoringEventPublisher.publishScoreCalculated(saved);

        log.info("Scored {} pts for player {} (phase={}, correct={})",
                points, playerId, phase, isCorrect);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Score> getRoundScores(String roomCode, int roundNumber) {
        return scoreRepository.findByRoomCodeAndRoundNumber(roomCode, roundNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Score> getMatchScores(String roomCode) {
        return scoreRepository.findByRoomCode(roomCode);
    }
}
