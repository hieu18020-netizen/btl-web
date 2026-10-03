package com.gamevui.backend.service;

import com.gamevui.backend.dto.CreateDuelRoomRequest;
import com.gamevui.backend.websocket.DuelRoomRegistry;
import com.gamevui.backend.websocket.LobbyRoomInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tuong duong /api/duel/create-room va /api/duel/rooms trong backend.py.
 */
@Service
public class DuelLobbyService {

    private final DuelRoomRegistry registry;
    private final long lobbyRoomTtlSeconds;

    public DuelLobbyService(DuelRoomRegistry registry,
                             @Value("${app.duel.lobby-room-ttl-seconds}") long lobbyRoomTtlSeconds) {
        this.registry = registry;
        this.lobbyRoomTtlSeconds = lobbyRoomTtlSeconds;
    }

    /** Tuong duong @app.post("/api/duel/create-room") */
    public Map<String, String> createRoom(CreateDuelRoomRequest req) {
        registry.purgeStaleLobbyRooms(lobbyRoomTtlSeconds);

        String game = req.gameOrDefault();
        if (game.length() > 20) {
            game = game.substring(0, 20);
        }
        String creatorName = req.creatorNameOrDefault();
        if (creatorName.length() > 30) {
            creatorName = creatorName.substring(0, 30);
        }

        String roomCode = registry.generateRoomCode();
        registry.lobbyRooms().put(roomCode, new LobbyRoomInfo(game, creatorName, System.currentTimeMillis()));
        return Map.of("room_code", roomCode);
    }

    /** Tuong duong @app.get("/api/duel/rooms") */
    public List<Map<String, Object>> listRooms(String game) {
        registry.purgeStaleLobbyRooms(lobbyRoomTtlSeconds);

        return registry.lobbyRooms().entrySet().stream()
                .filter(e -> game == null || game.isEmpty() || e.getValue().getGame().equals(game))
                .sorted(Comparator.comparingLong((Map.Entry<String, LobbyRoomInfo> e) -> e.getValue().getCreatedAtMillis()).reversed())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("room_code", e.getKey());
                    m.put("game", e.getValue().getGame());
                    m.put("creator_name", e.getValue().getCreatorName());
                    m.put("created_at", e.getValue().getCreatedAtMillis() / 1000.0);
                    return m;
                })
                .toList();
    }
}
