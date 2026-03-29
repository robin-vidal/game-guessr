package com.gameguessr.game.application.service;

import com.gameguessr.game.domain.exception.DuplicateGuessException;
import com.gameguessr.game.domain.exception.MatchAlreadyStartedException;
import com.gameguessr.game.domain.exception.MatchNotFoundException;
import com.gameguessr.game.domain.model.*;
import com.gameguessr.game.domain.port.outbound.GameEventPublisher;
import com.gameguessr.game.domain.port.outbound.LevelRepository;
import com.gameguessr.game.domain.port.outbound.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameApplicationService")
class GameApplicationServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private LevelRepository levelRepository;

    @Mock
    private GameEventPublisher gameEventPublisher;

    @InjectMocks
    private GameApplicationService service;

    private static final String ROOM_CODE = "ABC123";
    private static final String HOST_ID = "host-uuid-1";
    private static final String PLAYER_1 = "player-1";
    private static final String PLAYER_2 = "player-2";
    private static final List<String> PLAYER_IDS = List.of(PLAYER_1, PLAYER_2);
    private static final String GAME_PACK_SLUG = "mario-kart-wii";

    private Match waitingMatch;
    private List<Level> levels;

    @BeforeEach
    void setUp() {
        waitingMatch = Match.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .playerIds(List.of())
                .gamePack(GAME_PACK_SLUG)
                .status(MatchStatus.WAITING)
                .rounds(List.of())
                .currentRoundIndex(0)
                .build();

        levels = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            levels.add(Level.builder()
                    .id(UUID.randomUUID())
                    .gamePack(GAME_PACK_SLUG)
                    .levelName("Level " + (i + 1))
                    .coordinates(List.of(
                            LevelCoordinate.builder()
                                    .id(UUID.randomUUID())
                                    .noclipHash("hash-" + (i + 1))
                                    .spawnX(i * 10.0)
                                    .spawnZ(i * 20.0)
                                    .build()))
                    .build());
        }
    }

    // ── startMatch ────────────────────────────────────────────────────

    @Test
    @DisplayName("startMatch — succeeds when WAITING match exists with levels")
    void startMatch_waitingMatch_createsRoundsFromLevels() {
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(waitingMatch));
        when(levelRepository.findAll()).thenReturn(levels);
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Match result = service.startMatch(ROOM_CODE, HOST_ID, PLAYER_IDS);

        assertThat(result.getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(result.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(result.getRounds()).hasSize(5);
        assertThat(result.getPlayerIds()).containsExactlyElementsOf(PLAYER_IDS);
        verify(gameEventPublisher).publishRoundUpdate(eq(ROOM_CODE), any(Round.class));
    }

    @Test
    @DisplayName("startMatch — throws when match already IN_PROGRESS")
    void startMatch_alreadyInProgress_throws() {
        Match inProgress = waitingMatch.withStatus(MatchStatus.IN_PROGRESS);
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        assertThatThrownBy(() -> service.startMatch(ROOM_CODE, HOST_ID, PLAYER_IDS))
                .isInstanceOf(MatchAlreadyStartedException.class);
    }

    @Test
    @DisplayName("startMatch — throws when no match found")
    void startMatch_noMatch_throws() {
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startMatch(ROOM_CODE, HOST_ID, PLAYER_IDS))
                .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    @DisplayName("startMatch — rounds have level data")
    void startMatch_rounds_haveLevelData() {
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(waitingMatch));
        when(levelRepository.findAll()).thenReturn(levels);
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Match result = service.startMatch(ROOM_CODE, HOST_ID, PLAYER_IDS);

        result.getRounds().forEach(round -> {
            assertThat(round.getGamePackEntry().getGameId()).isEqualTo(GAME_PACK_SLUG);
            assertThat(round.getGamePackEntry().getNoclipHash()).isNotNull();
            assertThat(round.getGamePackEntry().getLevelId()).isNotNull();
        });
    }

    @Test
    @DisplayName("startMatch — throws when no levels available")
    void startMatch_noLevels_throws() {
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(waitingMatch));
        when(levelRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.startMatch(ROOM_CODE, HOST_ID, PLAYER_IDS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No levels available");
    }

    @Test
    @DisplayName("startMatch — uses unique levels for each round")
    void startMatch_uniqueLevelsPerRound() {
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(waitingMatch));
        when(levelRepository.findAll()).thenReturn(levels);
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Match result = service.startMatch(ROOM_CODE, HOST_ID, PLAYER_IDS);

        List<String> levelNames = result.getRounds().stream()
                .map(r -> r.getGamePackEntry().getLevelId())
                .toList();
        assertThat(levelNames).doesNotHaveDuplicates();
    }

    // ── getCurrentRound ────────────────────────────────────────────────

    @Test
    @DisplayName("getCurrentRound — returns first round")
    void getCurrentRound_returnsFirstRound() {
        Match inProgress = buildInProgressMatch();
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

    // ── submitGuess — single player does NOT advance (multiplayer) ────

    @Test
    @DisplayName("submitGuess — single player guess does NOT advance phase when 2 players in match")
    void submitGuess_singlePlayer_doesNotAdvance() {
        Match inProgress = buildInProgressMatch();
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Guess guess = Guess.builder()
                .playerId(PLAYER_1)
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart Wii")
                .submittedAt(Instant.now())
                .build();

        service.submitGuess(ROOM_CODE, guess);

        var savedMatch = captureAndReturnSavedMatch();
        assertThat(savedMatch.currentRound().getCurrentPhase()).isEqualTo(GuessPhase.GAME);
        assertThat(savedMatch.currentRound().getPhaseGuessedPlayerIds()).containsExactly(PLAYER_1);
    }

    @Test
    @DisplayName("submitGuess — all players guessing advances phase from GAME to LEVEL")
    void submitGuess_allPlayers_advancesPhase() {
        // Player 1 already guessed
        Match inProgress = buildInProgressMatchWithGuessedPlayers(GuessPhase.GAME, Set.of(PLAYER_1));
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Guess guess = Guess.builder()
                .playerId(PLAYER_2)
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart Wii")
                .submittedAt(Instant.now())
                .build();

        service.submitGuess(ROOM_CODE, guess);

        var savedMatch = captureAndReturnSavedMatch();
        assertThat(savedMatch.currentRound().getCurrentPhase()).isEqualTo(GuessPhase.LEVEL);
        assertThat(savedMatch.currentRound().getPhaseGuessedPlayerIds()).isEmpty();
    }

    @Test
    @DisplayName("submitGuess — duplicate guess throws DuplicateGuessException")
    void submitGuess_duplicateGuess_throws() {
        Match inProgress = buildInProgressMatchWithGuessedPlayers(GuessPhase.GAME, Set.of(PLAYER_1));
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        Guess guess = Guess.builder()
                .playerId(PLAYER_1)
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart Wii")
                .submittedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> service.submitGuess(ROOM_CODE, guess))
                .isInstanceOf(DuplicateGuessException.class)
                .hasMessageContaining(PLAYER_1);
    }

    @Test
    @DisplayName("submitGuess — phaseGuessedPlayerIds cleared after phase advance")
    void submitGuess_clearsGuessedIdsAfterAdvance() {
        Match inProgress = buildInProgressMatchWithGuessedPlayers(GuessPhase.LEVEL, Set.of(PLAYER_1));
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Guess guess = Guess.builder()
                .playerId(PLAYER_2)
                .phase(GuessPhase.LEVEL)
                .textAnswer("Luigi Circuit")
                .submittedAt(Instant.now())
                .build();

        service.submitGuess(ROOM_CODE, guess);

        var savedMatch = captureAndReturnSavedMatch();
        assertThat(savedMatch.currentRound().getCurrentPhase()).isEqualTo(GuessPhase.SPOT);
        assertThat(savedMatch.currentRound().getPhaseGuessedPlayerIds()).isEmpty();
    }

    @Test
    @DisplayName("submitGuess — full round cycle: GAME→LEVEL→SPOT→finished")
    void submitGuess_fullRoundCycle() {
        // Start at SPOT phase, player 1 already guessed
        Match inProgress = buildMultiRoundMatchWithGuessedPlayers(GuessPhase.SPOT, 0, Set.of(PLAYER_1));
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Guess guess = Guess.builder()
                .playerId(PLAYER_2)
                .phase(GuessPhase.SPOT)
                .guessX(100.0)
                .guessY(50.0)
                .guessZ(-200.0)
                .submittedAt(Instant.now())
                .build();

        service.submitGuess(ROOM_CODE, guess);

        var savedMatch = captureAndReturnSavedMatch();
        assertThat(savedMatch.getRounds().get(0).isFinished()).isTrue();
        assertThat(savedMatch.getCurrentRoundIndex()).isEqualTo(1);
        assertThat(savedMatch.isInProgress()).isTrue();
    }

    @Test
    @DisplayName("submitGuess — match finishes after last round's last SPOT")
    void submitGuess_lastRoundLastSpot_matchFinishes() {
        Match inProgress = buildMultiRoundMatchWithGuessedPlayers(GuessPhase.SPOT, 1, Set.of(PLAYER_1));
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Guess guess = Guess.builder()
                .playerId(PLAYER_2)
                .phase(GuessPhase.SPOT)
                .guessX(100.0)
                .guessY(50.0)
                .guessZ(-200.0)
                .submittedAt(Instant.now())
                .build();

        service.submitGuess(ROOM_CODE, guess);

        var savedMatch = captureAndReturnSavedMatch();
        assertThat(savedMatch.isFinished()).isTrue();
        assertThat(savedMatch.getStatus()).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("submitGuess — publishes event for GAME phase")
    void submitGuess_gamePhase_publishesEvent() {
        Match inProgress = buildInProgressMatch();
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Guess guess = Guess.builder()
                .playerId(PLAYER_1)
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart Wii")
                .submittedAt(Instant.now())
                .build();

        service.submitGuess(ROOM_CODE, guess);

        verify(gameEventPublisher).publishGuessSubmitted(eq(ROOM_CODE), eq(1), eq(guess),
                eq(GAME_PACK_SLUG), any(String.class));
    }

    @Test
    @DisplayName("submitGuess — publishes RoundUpdateEvent after progression")
    void submitGuess_publishesRoundUpdate() {
        Match inProgress = buildInProgressMatch();
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Guess guess = Guess.builder()
                .playerId(PLAYER_1)
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart Wii")
                .submittedAt(Instant.now())
                .build();

        service.submitGuess(ROOM_CODE, guess);

        verify(gameEventPublisher).publishRoundUpdate(eq(ROOM_CODE), any(Round.class));
    }

    @Test
    @DisplayName("submitGuess — throws InvalidPhaseException when LEVEL submitted before GAME")
    void submitGuess_levelBeforeGame_throws() {
        Match inProgress = buildInProgressMatch();
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        Guess guess = Guess.builder()
                .playerId(PLAYER_1)
                .phase(GuessPhase.LEVEL)
                .textAnswer("BabyPark")
                .submittedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> service.submitGuess(ROOM_CODE, guess))
                .isInstanceOf(com.gameguessr.game.domain.exception.InvalidPhaseException.class);
    }

    @Test
    @DisplayName("submitGuess — throws InvalidPhaseException when SPOT submitted before SPOT phase")
    void submitGuess_spotBeforeSpotPhase_throws() {
        Match inProgress = buildInProgressMatch();
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        Guess guess = Guess.builder()
                .playerId(PLAYER_1)
                .phase(GuessPhase.SPOT)
                .guessX(100.0)
                .guessY(50.0)
                .guessZ(-200.0)
                .submittedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> service.submitGuess(ROOM_CODE, guess))
                .isInstanceOf(com.gameguessr.game.domain.exception.InvalidPhaseException.class);
    }

    @Test
    @DisplayName("submitGuess — throws IllegalStateException when match is not in progress")
    void submitGuess_matchNotInProgress_throws() {
        Match waiting = buildInProgressMatch().withStatus(MatchStatus.WAITING);
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(waiting));

        Guess guess = Guess.builder()
                .playerId(PLAYER_1)
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart Wii")
                .submittedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> service.submitGuess(ROOM_CODE, guess))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("submitGuess — throws IllegalStateException when match is FINISHED")
    void submitGuess_matchFinished_throws() {
        Match finished = buildInProgressMatch().withStatus(MatchStatus.FINISHED);
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(finished));

        Guess guess = Guess.builder()
                .playerId(PLAYER_1)
                .phase(GuessPhase.GAME)
                .textAnswer("Mario Kart Wii")
                .submittedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> service.submitGuess(ROOM_CODE, guess))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in progress");
    }

    // ── getResults ────────────────────────────────────────────────────

    @Test
    @DisplayName("getResults — returns all rounds for the match")
    void getResults_returnsAllRounds() {
        Match inProgress = buildInProgressMatch();
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.of(inProgress));

        List<Round> result = service.getResults(ROOM_CODE);

        assertThat(result).hasSize(1);
        verify(matchRepository).findByRoomCode(ROOM_CODE);
    }

    @Test
    @DisplayName("getResults — throws MatchNotFoundException when room not found")
    void getResults_notFound_throws() {
        when(matchRepository.findByRoomCode(ROOM_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getResults(ROOM_CODE))
                .isInstanceOf(MatchNotFoundException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Match captureAndReturnSavedMatch() {
        ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        return captor.getValue();
    }

    private Round buildRound(int roundNumber, GuessPhase phase, boolean finished, Set<String> guessedPlayerIds) {
        return Round.builder()
                .id(UUID.randomUUID())
                .roundNumber(roundNumber)
                .gamePackEntry(GamePackEntry.builder()
                        .gameId(GAME_PACK_SLUG)
                        .levelId("Level " + roundNumber)
                        .noclipHash("hash-" + roundNumber)
                        .spawnX(0.0)
                        .spawnZ(0.0)
                        .build())
                .currentPhase(phase)
                .finished(finished)
                .startedAt(Instant.now().toEpochMilli())
                .phaseGuessedPlayerIds(guessedPlayerIds)
                .build();
    }

    private Match buildInProgressMatch() {
        return buildInProgressMatchWithGuessedPlayers(GuessPhase.GAME, new HashSet<>());
    }

    private Match buildInProgressMatchWithGuessedPlayers(GuessPhase phase, Set<String> guessedPlayerIds) {
        Round round = buildRound(1, phase, false, guessedPlayerIds);
        return Match.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .playerIds(PLAYER_IDS)
                .gamePack(GAME_PACK_SLUG)
                .status(MatchStatus.IN_PROGRESS)
                .rounds(List.of(round))
                .currentRoundIndex(0)
                .build();
    }

    private Match buildInProgressMatchAtPhase(GuessPhase phase) {
        return buildInProgressMatchWithGuessedPlayers(phase, new HashSet<>());
    }

    private Match buildMultiRoundMatchWithGuessedPlayers(GuessPhase phase, int currentRoundIndex, Set<String> guessedPlayerIds) {
        List<Round> rounds = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            rounds.add(buildRound(
                    i + 1,
                    i == currentRoundIndex ? phase : GuessPhase.GAME,
                    i < currentRoundIndex,
                    i == currentRoundIndex ? guessedPlayerIds : new HashSet<>()));
        }

        return Match.builder()
                .id(UUID.randomUUID())
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .playerIds(PLAYER_IDS)
                .gamePack(GAME_PACK_SLUG)
                .status(MatchStatus.IN_PROGRESS)
                .rounds(rounds)
                .currentRoundIndex(currentRoundIndex)
                .build();
    }

    private Match buildMultiRoundMatchAtPhase(GuessPhase phase, int currentRoundIndex) {
        return buildMultiRoundMatchWithGuessedPlayers(phase, currentRoundIndex, new HashSet<>());
    }
}
