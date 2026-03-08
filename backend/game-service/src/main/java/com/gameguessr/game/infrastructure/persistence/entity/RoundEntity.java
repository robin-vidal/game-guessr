package com.gameguessr.game.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * JPA entity for Round. Infrastructure layer only.
 */
@Entity
@Table(name = "rounds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    // ── GamePackEntry (embedded) ─────────────────────────────────────

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "level_id", nullable = false)
    private String levelId;

    /**
     * Spawn X coordinate — defaults to 0.0 (MVP placeholder).
     * TODO: populate from game pack data source.
     */
    @Column(name = "spawn_x", nullable = false)
    @Builder.Default
    private double spawnX = 0.0;

    @Column(name = "spawn_y", nullable = false)
    @Builder.Default
    private double spawnY = 0.0;

    @Column(name = "spawn_z", nullable = false)
    @Builder.Default
    private double spawnZ = 0.0;

    @Column(name = "current_phase", nullable = false, length = 10)
    private String currentPhase;

    @Column(name = "finished", nullable = false)
    private boolean finished;

    @Column(name = "started_at", nullable = false)
    private long startedAt;
}
