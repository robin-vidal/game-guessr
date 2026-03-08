package com.gameguessr.scoring.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI scoringOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scoring Service API")
                        .description("Score calculation and query endpoints for Game Guessr")
                        .version("0.0.1"));
    }
}
