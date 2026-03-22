package com.gameguessr.game.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for Match. Infrastructure layer only.
 */
@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "room_code", nullable = false, unique = true, length = 16)
    private String roomCode;

    @Column(name = "host_id", nullable = false)
    private String hostId;

    @Column(name = "game_pack", length = 64)
    private String gamePack;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "player_ids_csv", length = 1024)
    private String playerIdsCsv;

    @Column(name = "current_round_index", nullable = false)
    private int currentRoundIndex;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("roundNumber ASC")
    @Builder.Default
    private List<RoundEntity> rounds = new ArrayList<>();
}
