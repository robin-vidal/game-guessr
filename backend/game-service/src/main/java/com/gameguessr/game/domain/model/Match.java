package com.gameguessr.game.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.List;
import java.util.UUID;

/**
 * Match aggregate root.
 * Pure domain object — zero framework dependencies.
 */
@Getter
@Builder
@With
public class Match {

    private final UUID id;

    /** The room code from the Lobby Service (e.g. "ABC123"). */
    private final String roomCode;

    private final String hostId;

    /** IDs of all players in the match. */
    private final List<String> playerIds;

    /** Game pack slug (e.g. "mario-kart-wii"). */
    private final String gamePack;

    private final MatchStatus status;

    private final List<Round> rounds;

    /** Index into rounds (0-based). */
    private final int currentRoundIndex;

    // ── Business behaviour ──────────────────────────────────────────

    public Round currentRound() {
        if (rounds == null || currentRoundIndex >= rounds.size()) {
            throw new IllegalStateException("No active round found for match " + id);
        }
        return rounds.get(currentRoundIndex);
    }

    public boolean isFinished() {
        return status == MatchStatus.FINISHED;
    }

    public boolean isInProgress() {
        return status == MatchStatus.IN_PROGRESS;
    }
}
