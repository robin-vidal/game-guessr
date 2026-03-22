package com.gameguessr.scoring.application.service;

import com.gameguessr.scoring.domain.model.Score;
import com.gameguessr.scoring.domain.port.outbound.ScoreRepository;
import com.gameguessr.scoring.domain.port.outbound.ScoringEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScoringApplicationService")
class ScoringApplicationServiceTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private ScoringEventPublisher scoringEventPublisher;

    @InjectMocks
    private ScoringApplicationService service;

    private static final String ROOM_CODE = "ABC123";
    private static final String PLAYER_ID = "player-1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "gameCorrectPoints", 1000);
        ReflectionTestUtils.setField(service, "levelBasePoints", 500);
        ReflectionTestUtils.setField(service, "levelTimeBonusMax", 500);
        ReflectionTestUtils.setField(service, "spotMaxPoints", 5000);
    }

    @Test
    @DisplayName("scoreGuess — GAME correct gives 1000 points")
    void scoreGuess_gameCorrect_gives1000() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "Mario Kart 8", null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(1000);
        assertThat(result.isCorrect()).isTrue();
        verify(scoringEventPublisher).publishScoreCalculated(any(Score.class));
    }

    @Test
    @DisplayName("scoreGuess — GAME blank gives 0 points")
    void scoreGuess_gameBlank_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "", null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL correct gives base + time bonus")
    void scoreGuess_levelCorrect_givesBasePoints() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "BabyPark", null, null, null,
                Instant.now().toString());

        assertThat(result.getPoints()).isGreaterThanOrEqualTo(500);
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — SPOT is post-MVP, returns 0")
    void scoreGuess_spot_returns0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "SPOT", null, 100.0, 50.0, -200.0, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — publishes score.calculated event")
    void scoreGuess_publishesEvent() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "Mario Kart 8", null, null, null, null);

        verify(scoringEventPublisher).publishScoreCalculated(any(Score.class));
    }

    @Test
    @DisplayName("getRoundScores — delegates to repository")
    void getRoundScores_delegates() {
        when(scoreRepository.findByRoomCodeAndRoundNumber(ROOM_CODE, 1)).thenReturn(List.of());

        List<Score> result = service.getRoundScores(ROOM_CODE, 1);

        assertThat(result).isEmpty();
        verify(scoreRepository).findByRoomCodeAndRoundNumber(ROOM_CODE, 1);
    }

    @Test
    @DisplayName("getMatchScores — delegates to repository")
    void getMatchScores_delegates() {
        when(scoreRepository.findByRoomCode(ROOM_CODE)).thenReturn(List.of());

        List<Score> result = service.getMatchScores(ROOM_CODE);

        assertThat(result).isEmpty();
        verify(scoreRepository).findByRoomCode(ROOM_CODE);
    }

    @Test
    @DisplayName("scoreGuess — LEVEL with blank textAnswer gives 0 points")
    void scoreGuess_levelBlank_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "", null, null, null, Instant.now().toString());

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL with null submittedAt gives base points only")
    void scoreGuess_levelNullSubmittedAt_givesBasePoints() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "BabyPark", null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(500);
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL with invalid submittedAt falls back to base points")
    void scoreGuess_levelInvalidSubmittedAt_givesBasePoints() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "BabyPark", null, null, null, "not-a-timestamp");

        assertThat(result.getPoints()).isEqualTo(500);
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — unknown phase gives 0 points and false correct")
    void scoreGuess_unknownPhase_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "UNKNOWN", null, null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — null GAME textAnswer gives 0 points")
    void scoreGuess_gameNullTextAnswer_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", null, null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }
}
