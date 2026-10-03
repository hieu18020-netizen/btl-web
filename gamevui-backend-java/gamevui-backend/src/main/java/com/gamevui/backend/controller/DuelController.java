package com.gamevui.backend.controller;

import com.gamevui.backend.dto.CreateDuelRoomRequest;
import com.gamevui.backend.service.DuelLobbyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class DuelController {

    private final DuelLobbyService duelLobbyService;

    public DuelController(DuelLobbyService duelLobbyService) {
        this.duelLobbyService = duelLobbyService;
    }

    /** Tuong duong @app.post("/api/duel/create-room") */
    @PostMapping("/api/duel/create-room")
    public Map<String, String> createRoom(@RequestBody(required = false) CreateDuelRoomRequest request) {
        CreateDuelRoomRequest safeReq = request != null ? request : new CreateDuelRoomRequest(null, null);
        return duelLobbyService.createRoom(safeReq);
    }

    /** Tuong duong @app.get("/api/duel/rooms") */
    @GetMapping("/api/duel/rooms")
    public List<Map<String, Object>> listRooms(@RequestParam(required = false) String game) {
        return duelLobbyService.listRooms(game);
    }
}
