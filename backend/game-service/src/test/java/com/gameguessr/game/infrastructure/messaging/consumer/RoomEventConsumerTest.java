package com.gameguessr.game.infrastructure.messaging.consumer;

import com.gameguessr.game.domain.model.Match;
import com.gameguessr.game.domain.model.MatchStatus;
import com.gameguessr.game.domain.port.outbound.MatchRepository;
import com.gameguessr.game.infrastructure.messaging.event.RoomCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomEventConsumer")
class RoomEventConsumerTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private RoomEventConsumer consumer;

    private static final String ROOM_CODE = "ABC123";
    private static final String HOST_ID = "host-1";
    private static final String GAME_PACK = "mario-kart-wii";

    @Test
    @DisplayName("onRoomCreated — creates WAITING match with gamePack and empty rounds")
    void onRoomCreated_newRoom_createsWaitingMatch() {
        when(matchRepository.existsByRoomCode(ROOM_CODE)).thenReturn(false);

        consumer.onRoomCreated(buildEvent());

        ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        Match saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(MatchStatus.WAITING);
        assertThat(saved.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(saved.getHostId()).isEqualTo(HOST_ID);
        assertThat(saved.getGamePack()).isEqualTo(GAME_PACK);
        assertThat(saved.getRounds()).isEmpty();
    }

    @Test
    @DisplayName("onRoomCreated — ignores duplicate event (idempotency)")
    void onRoomCreated_duplicate_isIgnored() {
        when(matchRepository.existsByRoomCode(ROOM_CODE)).thenReturn(true);

        consumer.onRoomCreated(buildEvent());

        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("onRoomCreated — new match starts at round index 0")
    void onRoomCreated_newRoom_startsAtRoundZero() {
        when(matchRepository.existsByRoomCode(ROOM_CODE)).thenReturn(false);

        consumer.onRoomCreated(buildEvent());

        ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);
        verify(matchRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentRoundIndex()).isEqualTo(0);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private RoomCreatedEvent buildEvent() {
        RoomCreatedEvent event = new RoomCreatedEvent();
        event.setRoomCode(ROOM_CODE);
        event.setHostId(HOST_ID);
        event.setGamePack(GAME_PACK);
        return event;
    }
}
