package com.gameguessr.lobby.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lobbyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lobby Service API")
                        .description("Room management and player lobby endpoints for Game Guessr")
                        .version("0.0.1"));
    }
}
