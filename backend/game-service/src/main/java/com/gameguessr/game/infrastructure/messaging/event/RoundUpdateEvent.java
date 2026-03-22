package com.gameguessr.game.infrastructure.messaging.event;

import lombok.Builder;
import lombok.Data;

/**
 * Kafka event payload for {@code game.round.update}.
 * Consumed by the WebSocket Gateway for real-time sync.
 */
@Data
@Builder
public class RoundUpdateEvent {

    private String roomCode;
    private int roundNumber;
    private String currentPhase;
    private boolean finished;
    private long startedAt;
    private String gameId;
    private String levelId;
    private String noclipHash;
}
