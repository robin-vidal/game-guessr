package com.gameguessr.game.infrastructure.messaging.consumer;

import com.gameguessr.game.domain.model.Match;
import com.gameguessr.game.domain.model.MatchStatus;
import com.gameguessr.game.domain.port.outbound.MatchRepository;
import com.gameguessr.game.infrastructure.messaging.event.RoomCreatedEvent;
import com.gameguessr.game.domain.model.Round;
import com.gameguessr.game.domain.model.GamePackEntry;
import com.gameguessr.game.domain.model.GuessPhase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Consumes {@code game.room.events} from the Lobby Service.
 * Creates a WAITING match record in advance, ready to be started by the host.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventConsumer {

    private static final int ROUNDS_PER_MATCH = 5;

    private final MatchRepository matchRepository;

    @KafkaListener(topics = "${kafka.topics.room-events}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void onRoomCreated(RoomCreatedEvent event) {
        log.info("Received room.created event for room {}", event.getRoomCode());

        if (matchRepository.existsByRoomCode(event.getRoomCode())) {
            log.warn("Match already exists for room {} — ignoring duplicate event", event.getRoomCode());
            return;
        }

        // Pre-create a WAITING match so it is ready when the host hits /start
        List<Round> rounds = buildPlaceholderRounds();

        Match match = Match.builder()
                .id(UUID.randomUUID())
                .roomCode(event.getRoomCode())
                .hostId(event.getHostId())
                .status(MatchStatus.WAITING)
                .rounds(rounds)
                .currentRoundIndex(0)
                .build();

        matchRepository.save(match);
        log.info("Pre-created WAITING match for room {}", event.getRoomCode());
    }

    private List<Round> buildPlaceholderRounds() {
        List<Round> rounds = new ArrayList<>();
        for (int i = 1; i <= ROUNDS_PER_MATCH; i++) {
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
