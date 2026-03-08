package com.gameguessr.lobby.domain.port.inbound;

import com.gameguessr.lobby.domain.model.Room;
import com.gameguessr.lobby.domain.model.RoomSettings;

import java.util.List;

/**
 * Inbound port — defines all use cases for the Lobby Service.
 * Implemented by the application layer. Consumed by the REST adapter.
 */
public interface LobbyUseCase {

    /**
     * Creates a new room and publishes a room.created event to Kafka.
     *
     * @param hostId    the userId of the host creating the room
     * @param isPrivate whether the room is private (invite-only)
     * @return the newly created {@link Room}
     */
    Room createRoom(String hostId, boolean isPrivate);

    /**
     * Returns room details including its players and settings.
     *
     * @param roomCode the room code
     * @return the {@link Room}
     */
    Room getRoom(String roomCode);

    /**
     * Updates room settings (round count, time limit, game pack).
     * Only the host can update settings.
     *
     * @param roomCode the room code
     * @param playerId the userId requesting the change (must be host)
     * @param settings the new settings to apply
     * @return the updated {@link Room}
     */
    Room updateSettings(String roomCode, String playerId, RoomSettings settings);

    /**
     * Adds a player to the room.
     *
     * @param roomCode    the room code
     * @param playerId    the userId of the joining player
     * @param displayName the display name
     * @return the updated {@link Room}
     */
    Room joinRoom(String roomCode, String playerId, String displayName);

    /**
     * Removes a player from the room.
     * If the host leaves, the room is closed.
     *
     * @param roomCode the room code
     * @param playerId the userId of the leaving player
     */
    void leaveRoom(String roomCode, String playerId);

    /**
     * Lists all rooms with OPEN status.
     *
     * @return list of open {@link Room}s
     */
    List<Room> findOpenRooms();
}
