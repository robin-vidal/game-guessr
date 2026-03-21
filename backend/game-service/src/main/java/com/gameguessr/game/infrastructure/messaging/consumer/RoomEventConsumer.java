package com.gameguessr.game.infrastructure.messaging.consumer;

import com.gameguessr.game.domain.model.Match;
import com.gameguessr.game.domain.model.MatchStatus;
import com.gameguessr.game.domain.port.outbound.MatchRepository;
import com.gameguessr.game.infrastructure.messaging.event.RoomCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventConsumer {

    private final MatchRepository matchRepository;

    @KafkaListener(topics = "${kafka.topics.room-events}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void onRoomCreated(RoomCreatedEvent event) {
        log.info("Received room.created event for room {}", event.getRoomCode());

        if (matchRepository.existsByRoomCode(event.getRoomCode())) {
            log.warn("Match already exists for room {} — ignoring duplicate event", event.getRoomCode());
            return;
        }

        Match match = Match.builder()
                .id(UUID.randomUUID())
                .roomCode(event.getRoomCode())
                .hostId(event.getHostId())
                .gamePack(event.getGamePack())
                .status(MatchStatus.WAITING)
                .rounds(List.of())
                .currentRoundIndex(0)
                .build();

        matchRepository.save(match);
        log.info("Pre-created WAITING match for room {} with gamePack={}", event.getRoomCode(), event.getGamePack());
    }
}
