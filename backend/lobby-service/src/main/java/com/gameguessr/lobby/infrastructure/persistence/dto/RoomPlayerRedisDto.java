package com.gameguessr.lobby.infrastructure.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis serialization DTO for RoomPlayer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlayerRedisDto {

    private String playerId;
    private String displayName;
    private String joinedAt;
}
