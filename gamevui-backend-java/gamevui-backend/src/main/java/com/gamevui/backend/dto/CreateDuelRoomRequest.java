package com.gamevui.backend.dto;

/** Tuong duong class CreateDuelRoom(BaseModel) trong backend.py, co gia tri mac dinh. */
public record CreateDuelRoomRequest(String game, String creatorName) {
    public String gameOrDefault() {
        String g = (game == null) ? "" : game.strip();
        return g.isEmpty() ? "chess" : g;
    }

    public String creatorNameOrDefault() {
        String c = (creatorName == null) ? "" : creatorName.strip();
        return c.isEmpty() ? "\u1EA8n danh" : c;
    }
}
