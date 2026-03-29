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
    private static final String CORRECT_GAME_ID = "mario-kart-wii";
    private static final String CORRECT_LEVEL_ID = "Luigi Circuit";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "gameCorrectPoints", 1000);
        ReflectionTestUtils.setField(service, "levelBasePoints", 500);
        ReflectionTestUtils.setField(service, "levelTimeBonusMax", 500);
        ReflectionTestUtils.setField(service, "spotMaxPoints", 5000);
    }

    // ── GAME phase ────────────────────────────────────────────────────

    @Test
    @DisplayName("scoreGuess — GAME exact match gives 1000 points")
    void scoreGuess_gameExactMatch_gives1000() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "mario kart wii",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(1000);
        assertThat(result.isCorrect()).isTrue();
        verify(scoringEventPublisher).publishScoreCalculated(any(Score.class));
    }

    @Test
    @DisplayName("scoreGuess — GAME case-insensitive match gives 1000 points")
    void scoreGuess_gameCaseInsensitive_gives1000() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "Mario Kart Wii",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(1000);
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — GAME 1-char typo gives 1000 points (fuzzy)")
    void scoreGuess_gameOneCharTypo_gives1000() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // "mario kart wi" is 1 char shorter than "mario kart wii" → levenshtein = 1
        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "mario kart wi",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(1000);
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — GAME 3-char typo gives 0 points")
    void scoreGuess_gameThreeCharTypo_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // "mario kart" is 4 chars shorter than "mario kart wii" → levenshtein = 4
        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "mario kart",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — GAME completely wrong answer gives 0 points")
    void scoreGuess_gameWrongAnswer_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "tetris",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — GAME blank gives 0 points")
    void scoreGuess_gameBlank_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — GAME null textAnswer gives 0 points")
    void scoreGuess_gameNullTextAnswer_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", null,
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    // ── LEVEL phase ───────────────────────────────────────────────────

    @Test
    @DisplayName("scoreGuess — LEVEL exact match gives base + time bonus")
    void scoreGuess_levelExactMatch_givesBasePoints() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "Luigi Circuit",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, Instant.now().toString());

        assertThat(result.getPoints()).isGreaterThanOrEqualTo(500);
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL case-insensitive match is correct")
    void scoreGuess_levelCaseInsensitive_isCorrect() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "luigi circuit",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL 1-char typo is correct")
    void scoreGuess_levelOneCharTypo_isCorrect() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // "Luigi Circui" → levenshtein("luigi circui", "luigi circuit") = 1
        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "Luigi Circui",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL completely wrong answer gives 0")
    void scoreGuess_levelWrongAnswer_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "Baby Park",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL with blank textAnswer gives 0 points")
    void scoreGuess_levelBlank_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, Instant.now().toString());

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL with null submittedAt gives base points only")
    void scoreGuess_levelNullSubmittedAt_givesBasePoints() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "Luigi Circuit",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(500);
        assertThat(result.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("scoreGuess — LEVEL with invalid submittedAt falls back to base points")
    void scoreGuess_levelInvalidSubmittedAt_givesBasePoints() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "LEVEL", "Luigi Circuit",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, "not-a-timestamp");

        assertThat(result.getPoints()).isEqualTo(500);
        assertThat(result.isCorrect()).isTrue();
    }

    // ── SPOT phase ────────────────────────────────────────────────────

    @Test
    @DisplayName("scoreGuess — SPOT is post-MVP, returns 0")
    void scoreGuess_spot_returns0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "SPOT", null,
                null, null,
                100.0, 50.0, -200.0, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
    }

    // ── Other ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("scoreGuess — publishes score.calculated event")
    void scoreGuess_publishesEvent() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "GAME", "mario kart wii",
                CORRECT_GAME_ID, CORRECT_LEVEL_ID,
                null, null, null, null);

        verify(scoringEventPublisher).publishScoreCalculated(any(Score.class));
    }

    @Test
    @DisplayName("scoreGuess — unknown phase gives 0 points and false correct")
    void scoreGuess_unknownPhase_gives0() {
        when(scoreRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Score result = service.scoreGuess(ROOM_CODE, 1, PLAYER_ID,
                "UNKNOWN", null,
                null, null,
                null, null, null, null);

        assertThat(result.getPoints()).isEqualTo(0);
        assertThat(result.isCorrect()).isFalse();
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
}
