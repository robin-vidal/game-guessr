package com.gameguessr.game.domain.port.outbound;

import com.gameguessr.game.domain.model.Level;

import java.util.List;

public interface LevelRepository {

    List<Level> findAll();

    Level save(Level level);

    long count();
}
