package com.gameguessr.game.domain.port.outbound;

import com.gameguessr.game.domain.model.Match;

import java.util.Optional;

/**
 * Outbound port — persistence contract for Match aggregates.
 * Implemented by the JPA adapter in the infrastructure layer.
 */
public interface MatchRepository {

    /** Persist or update a match. */
    Match save(Match match);

    /** Find a match by room code. */
    Optional<Match> findByRoomCode(String roomCode);

    /** Check whether a match exists for a given room code. */
    boolean existsByRoomCode(String roomCode);
}
