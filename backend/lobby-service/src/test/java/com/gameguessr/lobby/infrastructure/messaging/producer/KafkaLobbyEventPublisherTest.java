package com.gameguessr.lobby.infrastructure.messaging.producer;

import com.gameguessr.lobby.domain.model.*;
import com.gameguessr.lobby.infrastructure.messaging.event.RoomCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaLobbyEventPublisher")
class KafkaLobbyEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaLobbyEventPublisher publisher;

    private static final String ROOM_CODE = "ABC123";
    private static final String HOST_ID = "host-uuid-1";
    private static final String ROOM_EVENTS_TOPIC = "game.room.events";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        ReflectionTestUtils.setField(publisher, "roomEventsTopic", ROOM_EVENTS_TOPIC);
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("publishRoomCreated — sends event with gamePack from room settings")
    void publishRoomCreated_sendsCorrectEvent() {
        Room room = buildRoom();

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        publisher.publishRoomCreated(room);

        verify(kafkaTemplate).send(eq(ROOM_EVENTS_TOPIC), eq(ROOM_CODE), eventCaptor.capture());
        RoomCreatedEvent event = (RoomCreatedEvent) eventCaptor.getValue();
        assertThat(event.getRoomCode()).isEqualTo(ROOM_CODE);
        assertThat(event.getHostId()).isEqualTo(HOST_ID);
        assertThat(event.isPrivate()).isFalse();
        assertThat(event.getGamePack()).isEqualTo("mario-kart-wii");
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Room buildRoom() {
        List<RoomPlayer> players = new ArrayList<>();
        players.add(RoomPlayer.builder()
                .playerId(HOST_ID)
                .displayName("Host")
                .joinedAt(Instant.now())
                .build());

        return Room.builder()
                .roomCode(ROOM_CODE)
                .hostId(HOST_ID)
                .isPrivate(false)
                .status(RoomStatus.OPEN)
                .settings(RoomSettings.builder().build())
                .maxPlayers(8)
                .players(players)
                .createdAt(Instant.now())
                .build();
    }
}
