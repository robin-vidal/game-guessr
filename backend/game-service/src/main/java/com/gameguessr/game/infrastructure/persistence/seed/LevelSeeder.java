package com.gameguessr.game.infrastructure.persistence.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameguessr.game.domain.model.Level;
import com.gameguessr.game.domain.model.LevelCoordinate;
import com.gameguessr.game.domain.port.outbound.LevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LevelSeeder implements ApplicationRunner {

    private final LevelRepository levelRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (levelRepository.count() > 0) {
            log.info("Levels already seeded — skipping");
            return;
        }

        InputStream is = new ClassPathResource("game-packs.json").getInputStream();
        List<Map<String, Object>> raw = objectMapper.readValue(is, new TypeReference<>() {});

        int levelCount = 0;
        for (Map<String, Object> entry : raw) {
            String gameName = (String) entry.get("game");
            String slug = slugify(gameName);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> positions = (List<Map<String, Object>>) entry.get("positions");

            // Group positions by level name
            Map<String, List<Map<String, Object>>> byLevel = new LinkedHashMap<>();
            for (Map<String, Object> p : positions) {
                String levelName = (String) p.get("level");
                byLevel.computeIfAbsent(levelName, k -> new ArrayList<>()).add(p);
            }

            for (Map.Entry<String, List<Map<String, Object>>> levelEntry : byLevel.entrySet()) {
                List<LevelCoordinate> coordinates = levelEntry.getValue().stream()
                        .map(p -> LevelCoordinate.builder()
                                .id(UUID.randomUUID())
                                .noclipHash((String) p.get("hash"))
                                .spawnX(((Number) p.get("x")).doubleValue())
                                .spawnZ(((Number) p.get("z")).doubleValue())
                                .build())
                        .toList();

                Level level = Level.builder()
                        .id(UUID.randomUUID())
                        .gamePack(slug)
                        .levelName(levelEntry.getKey())
                        .coordinates(coordinates)
                        .build();

                levelRepository.save(level);
                levelCount++;
            }
        }

        log.info("Seeded {} levels", levelCount);
    }

    static String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
