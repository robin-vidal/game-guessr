package com.gameguessr.auth.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Game-Guessr — Auth Service API")
                        .description("Manages user authentication and JWT tokens.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Game-Guessr Team")
                                .email("team@gameguessr.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Dev"),
                        new Server().url("http://auth-service:8081").description("Docker / K8s")));
    }
}