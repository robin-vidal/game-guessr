package com.gameguessr.game.application.service;

import com.gameguessr.game.domain.exception.MatchAlreadyStartedException;
import com.gameguessr.game.domain.exception.MatchNotFoundException;
import com.gameguessr.game.domain.model.*;
import com.gameguessr.game.domain.port.outbound.GameEventPublisher;
import com.gameguessr.game.domain.port.outbound.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameApplicationService")
class GameApplicationServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private GameEventPublisher gameEventPublisher;

    @InjectMocks
    private GameApplicationService service;

    private static final String ROOM_CODE = "ABC123";
    private static final String HOST_ID = "host-uuid-1";

    private Match waitingMatch;

    @BeforeEach
    void setUp() {
        List<Round> rounds = buildRounds();
        waitingMatch = Match.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .status(MatchStatus.WAITING)
                .rounds(rounds)
                .currentRoundIndex(0)
                .build();
    }

    // ── startMatch ────────────────────────────────────────────────────

    @Test
    @DisplayName("startMatch — succeeds when room has no existing match")
    void startMatch_noExistingMatch_createsNewMatch() {
        when(matchRepository.existsByRoomCode(ROOM_CODE)).thenReturn(false);
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Match result = service.startMatch(ROOM_CODE, HOST_ID);

        assertThat(result.getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(result.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(result.getRounds()).hasSize(5);
        verify(gameEventPublisher).publishRoundUpdate(eq(ROOM_CODE), any(Round.class));
    }

    @Test
    @DisplayName("startMatch — throws when match already IN_PROGRESS")
    void startMatch_alreadyInProgress_throws() {
        Match inProgress = waitingMatch.withStatus(MatchStatus.IN_PROGRESS);
        when(matchRepository.existsByRoomCode(ROOM_CODE)).thenReturn(true);
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        assertThatThrownBy(() -> service.startMatch(ROOM_CODE, HOST_ID))
                .isInstanceOf(MatchAlreadyStartedException.class);
    }

    @Test
    @DisplayName("startMatch — round coordinates default to (0, 0, 0)")
    void startMatch_rounds_defaultCoordinatesAreZero() {
        when(matchRepository.existsByRoomCode(ROOM_CODE)).thenReturn(false);
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Match result = service.startMatch(ROOM_CODE, HOST_ID);

        result.getRounds().forEach(round -> {
            assertThat(round.getGamePackEntry().getSpawnX()).isEqualTo(0.0);
            assertThat(round.getGamePackEntry().getSpawnY()).isEqualTo(0.0);
            assertThat(round.getGamePackEntry().getSpawnZ()).isEqualTo(0.0);
        });
    }

    // ── getCurrentRound ────────────────────────────────────────────────

    @Test
    @DisplayName("getCurrentRound — returns first round")
    void getCurrentRound_returnsFirstRound() {
        Match inProgress = waitingMatch.withStatus(MatchStatus.IN_PROGRESS);
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        Round round = service.getCurrentRound(ROOM_CODE);

        assertThat(round.getRoundNumber()).isEqualTo(1);
        assertThat(round.getCurrentPhase()).isEqualTo(GuessPhase.GAME);
    }

    @Test
    @DisplayName("getCurrentRound — throws when match not found")
    void getCurrentRound_notFound_throws() {
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentRound(ROOM_CODE))
                .isInstanceOf(MatchNotFoundException.class);
    }

    // ── submitGuess ────────────────────────────────────────────────────

    @Test
    @DisplayName("submitGuess — publishes event for GAME phase")
    void submitGuess_gamePhase_publishesEvent() {
        Match inProgress = waitingMatch.withStatus(MatchStatus.IN_PROGRESS);
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        Guess guess = Guess.builder()
                .playerId("player-1")
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart 8")
                .submittedAt(Instant.now())
                .build();

        service.submitGuess(ROOM_CODE, guess);

        verify(gameEventPublisher).publishGuessSubmitted(eq(ROOM_CODE), eq(1), eq(guess));
    }

    @Test
    @DisplayName("submitGuess — throws InvalidPhaseException when LEVEL submitted before GAME")
    void submitGuess_levelBeforeGame_throws() {
        Match inProgress = waitingMatch.withStatus(MatchStatus.IN_PROGRESS);
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        Guess guess = Guess.builder()
                .playerId("player-1")
                .phase(GuessPhase.LEVEL)
                .textAnswer("BabyPark")
                .submittedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> service.submitGuess(ROOM_CODE, guess))
                .isInstanceOf(com.gameguessr.game.domain.exception.InvalidPhaseException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private List<Round> buildRounds() {
        List<Round> rounds = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            rounds.add(Round.builder()
                    .id(UUID.randomUUID())
                    .roundNumber(i)
                    .gamePackEntry(GamePackEntry.builder()
                            .gameId("mario-kart-8")
                            .levelId("TBD-" + i)
                            .build())
                    .currentPhase(GuessPhase.GAME)
                    .finished(false)
                    .startedAt(Instant.now().toEpochMilli())
                    .build());
        }
        return rounds;
    }
}
