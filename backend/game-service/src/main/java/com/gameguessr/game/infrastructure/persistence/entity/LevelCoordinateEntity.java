package com.gameguessr.game.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "level_coordinates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelCoordinateEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private LevelEntity level;

    @Column(name = "noclip_hash", nullable = false, length = 512)
    private String noclipHash;

    @Column(name = "spawn_x", nullable = false)
    @Builder.Default
    private double spawnX = 0.0;

    @Column(name = "spawn_z", nullable = false)
    @Builder.Default
    private double spawnZ = 0.0;
}
