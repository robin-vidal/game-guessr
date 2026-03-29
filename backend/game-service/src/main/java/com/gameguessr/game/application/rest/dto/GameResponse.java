package com.gameguessr.game.application.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameResponse {
    private String slug;
    private String displayName;
}
