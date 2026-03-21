package com.gameguessr.lobby.infrastructure.messaging.producer;

import com.gameguessr.lobby.domain.model.Room;
import com.gameguessr.lobby.domain.port.outbound.LobbyEventPublisher;
import com.gameguessr.lobby.infrastructure.messaging.event.RoomCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Driven adapter — implements LobbyEventPublisher port using Apache Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaLobbyEventPublisher implements LobbyEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.room-events}")
    private String roomEventsTopic;

    @Override
    public void publishRoomCreated(Room room) {
        RoomCreatedEvent event = RoomCreatedEvent.builder()
                .roomCode(room.getRoomCode())
                .hostId(room.getHostId())
                .isPrivate(room.isPrivate())
                .gamePack(room.getSettings() != null ? room.getSettings().getGamePack() : "mario-kart-wii")
                .build();

        kafkaTemplate.send(roomEventsTopic, room.getRoomCode(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish room.created event for room {}: {}",
                                room.getRoomCode(), ex.getMessage());
                    } else {
                        log.debug("Published room.created event to topic {} [room={}]",
                                roomEventsTopic, room.getRoomCode());
                    }
                });
    }
}
