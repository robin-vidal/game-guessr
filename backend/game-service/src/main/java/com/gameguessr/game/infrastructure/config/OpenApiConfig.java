package com.gameguessr.game.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI / Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gameServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Game-Guessr — Game Service API")
                        .description("Manages match lifecycle, round timers, and player guess submission.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Game-Guessr Team")
                                .email("team@gameguessr.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local Dev"),
                        new Server().url("http://game-service:8082").description("Docker / K8s")));
    }
}
