package com.gameguessr.game.domain.port.outbound;

import com.gameguessr.game.domain.model.Level;

import java.util.List;

public interface LevelRepository {

    List<Level> findAll();

    Level save(Level level);

    long count();

    /** Returns distinct game pack slugs (e.g. "mario-kart-wii"). */
    List<String> findGamePacks();

    /**
     * Returns level names containing {@code query} (case-insensitive), max 8 results.
     */
    List<String> findLevelNames(String query);
}
