package com.gameguessr.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Match")
class MatchTest {

    @Test
    @DisplayName("currentRound() — returns round at currentRoundIndex")
    void currentRound_returnsRoundAtIndex() {
        Round r1 = buildRound(1);
        Round r2 = buildRound(2);
        Match match = buildMatch(List.of(r1, r2), 1);

        assertThat(match.currentRound().getRoundNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("currentRound() — returns first round when index is 0")
    void currentRound_indexZero_returnsFirstRound() {
        Round r1 = buildRound(1);
        Match match = buildMatch(List.of(r1), 0);

        assertThat(match.currentRound().getRoundNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("currentRound() — throws IllegalStateException when index out of bounds")
    void currentRound_outOfBounds_throws() {
        Match match = buildMatch(List.of(buildRound(1)), 5);

        assertThatThrownBy(match::currentRound)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No active round found");
    }

    @Test
    @DisplayName("currentRound() — throws when rounds is null")
    void currentRound_nullRounds_throws() {
        Match match = Match.builder()
                .id(UUID.randomUUID())
                .roomCode("ABC123")
                .hostId("host-1")
                .status(MatchStatus.IN_PROGRESS)
                .rounds(null)
                .currentRoundIndex(0)
                .build();

        assertThatThrownBy(match::currentRound)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("isFinished() — true when status is FINISHED")
    void isFinished_finishedStatus_returnsTrue() {
        Match match = buildMatch(List.of(), 0).withStatus(MatchStatus.FINISHED);
        assertThat(match.isFinished()).isTrue();
    }

    @Test
    @DisplayName("isFinished() — false when status is IN_PROGRESS")
    void isFinished_inProgress_returnsFalse() {
        Match match = buildMatch(List.of(), 0).withStatus(MatchStatus.IN_PROGRESS);
        assertThat(match.isFinished()).isFalse();
    }

    @Test
    @DisplayName("isInProgress() — true when status is IN_PROGRESS")
    void isInProgress_inProgressStatus_returnsTrue() {
        Match match = buildMatch(List.of(), 0).withStatus(MatchStatus.IN_PROGRESS);
        assertThat(match.isInProgress()).isTrue();
    }

    @Test
    @DisplayName("isInProgress() — false when status is WAITING")
    void isInProgress_waiting_returnsFalse() {
        Match match = buildMatch(List.of(), 0).withStatus(MatchStatus.WAITING);
        assertThat(match.isInProgress()).isFalse();
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Round buildRound(int number) {
        return Round.builder()
                .id(UUID.randomUUID())
                .roundNumber(number)
                .gamePackEntry(GamePackEntry.builder()
                        .gameId("mario-kart-wii")
                        .levelId("Luigi Circuit")
                        .noclipHash("mkwii/beginner_course;ShareData=test")
                        .build())
                .currentPhase(GuessPhase.GAME)
                .finished(false)
                .startedAt(System.currentTimeMillis())
                .build();
    }

    private Match buildMatch(List<Round> rounds, int currentRoundIndex) {
        return Match.builder()
                .id(UUID.randomUUID())
                .roomCode("ABC123")
                .hostId("host-1")
                .gamePack("mario-kart-wii")
                .status(MatchStatus.IN_PROGRESS)
                .rounds(rounds)
                .currentRoundIndex(currentRoundIndex)
                .build();
    }
}
