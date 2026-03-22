package com.gameguessr.game.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "levels", uniqueConstraints = @UniqueConstraint(columnNames = {"game_pack", "level_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "game_pack", nullable = false, length = 128)
    private String gamePack;

    @Column(name = "level_name", nullable = false, length = 128)
    private String levelName;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<LevelCoordinateEntity> coordinates = new ArrayList<>();
}
