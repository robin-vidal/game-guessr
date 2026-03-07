package com.gameguessr.leaderboard.application.service;

import com.gameguessr.leaderboard.domain.model.LeaderboardEntry;
import com.gameguessr.leaderboard.domain.port.outbound.LeaderboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaderboardApplicationService")
class LeaderboardApplicationServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @InjectMocks
    private LeaderboardApplicationService service;

    private static final String ROOM_CODE = "ABC123";
    private static final String PLAYER_ID = "player-1";
    private static final String GLOBAL_KEY = "leaderboard:global";
    private static final String ROOM_KEY_PREFIX = "leaderboard:room:";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "globalKey", GLOBAL_KEY);
        ReflectionTestUtils.setField(service, "roomKeyPrefix", ROOM_KEY_PREFIX);
        ReflectionTestUtils.setField(service, "defaultTopN", 100);
    }

    @Test
    @DisplayName("recordScore — updates both global and room leaderboards")
    void recordScore_updatesBothLeaderboards() {
        service.recordScore(ROOM_CODE, PLAYER_ID, 1000);

        verify(leaderboardRepository).incrementScore(GLOBAL_KEY, PLAYER_ID, 1000.0);
        verify(leaderboardRepository).incrementScore(ROOM_KEY_PREFIX + ROOM_CODE, PLAYER_ID, 1000.0);
    }

    @Test
    @DisplayName("getGlobalLeaderboard — returns top-N entries")
    void getGlobalLeaderboard_returnsEntries() {
        List<LeaderboardEntry> expected = List.of(
                LeaderboardEntry.builder().playerId("p1").score(3000).rank(1).build(),
                LeaderboardEntry.builder().playerId("p2").score(2000).rank(2).build());
        when(leaderboardRepository.getTopN(GLOBAL_KEY, 10)).thenReturn(expected);

        List<LeaderboardEntry> result = service.getGlobalLeaderboard(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlayerId()).isEqualTo("p1");
        assertThat(result.get(0).getScore()).isEqualTo(3000);
    }

    @Test
    @DisplayName("getRoomLeaderboard — returns room-specific rankings")
    void getRoomLeaderboard_returnsEntries() {
        when(leaderboardRepository.getTopN(ROOM_KEY_PREFIX + ROOM_CODE, 100))
                .thenReturn(List.of());

        List<LeaderboardEntry> result = service.getRoomLeaderboard(ROOM_CODE);

        assertThat(result).isEmpty();
        verify(leaderboardRepository).getTopN(ROOM_KEY_PREFIX + ROOM_CODE, 100);
    }

    @Test
    @DisplayName("getGlobalLeaderboard — negative topN uses default")
    void getGlobalLeaderboard_negativeTopN_usesDefault() {
        when(leaderboardRepository.getTopN(GLOBAL_KEY, 100)).thenReturn(List.of());

        service.getGlobalLeaderboard(-1);

        verify(leaderboardRepository).getTopN(GLOBAL_KEY, 100);
    }
}
