package com.gamevui.backend.websocket;

/** Tuong duong 1 gia tri trong dict LOBBY_ROOMS cua backend.py. */
public class LobbyRoomInfo {
    private final String game;
    private final String creatorName;
    private final long createdAtMillis;

    public LobbyRoomInfo(String game, String creatorName, long createdAtMillis) {
        this.game = game;
        this.creatorName = creatorName;
        this.createdAtMillis = createdAtMillis;
    }

    public String getGame() { return game; }
    public String getCreatorName() { return creatorName; }
    public long getCreatedAtMillis() { return createdAtMillis; }
}
