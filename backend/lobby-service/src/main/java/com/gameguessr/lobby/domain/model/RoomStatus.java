package com.gameguessr.lobby.domain.model;

/**
 * Lifecycle states for a Room.
 */
public enum RoomStatus {
    /** Room is open and accepting players. */
    OPEN,
    /** Room has reached max players. */
    FULL,
    /** Match is in progress. */
    IN_GAME,
    /** Room has been closed / disbanded. */
    CLOSED
}
