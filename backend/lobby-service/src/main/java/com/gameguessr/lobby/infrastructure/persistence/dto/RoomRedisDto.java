package com.gameguessr.lobby.infrastructure.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Redis serialization DTO for Room.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRedisDto {

    private String roomCode;
    private String hostId;
    private boolean isPrivate;
    private String status;
    private int maxPlayers;
    private int roundCount;
    private int timeLimitSeconds;
    private String gamePack;
    private List<RoomPlayerRedisDto> players;
    private String createdAt;
}
