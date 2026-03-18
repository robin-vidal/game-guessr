package com.gameguessr.scoring.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "room_code", nullable = false, length = 16)
    private String roomCode;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "player_id", nullable = false)
    private String playerId;

    @Column(name = "phase", nullable = false, length = 10)
    private String phase;

    @Column(name = "points", nullable = false)
    private int points;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "time_bonus_ms")
    @Builder.Default
    private long timeBonusMs = 0;

    @Column(name = "distance_error")
    @Builder.Default
    private double distanceError = 0.0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
