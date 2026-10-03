package com.gamevui.backend.controller;

import com.gamevui.backend.dto.GameScoreRequest;
import com.gamevui.backend.security.CurrentUsername;
import com.gamevui.backend.service.GameSessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GameController {

    private final GameSessionService gameSessionService;

    public GameController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    /** Tuong duong @app.post("/api/start-game") */
    @PostMapping("/api/start-game")
    public Map<String, String> startGame(@CurrentUsername String username) {
        return Map.of("session_token", gameSessionService.startGame(username));
    }

    /** Tuong duong @app.post("/api/update-score") */
    @PostMapping("/api/update-score")
    public Map<String, Object> updateScore(@RequestBody GameScoreRequest request,
                                             @CurrentUsername String username) {
        int highScore = gameSessionService.updateScore(username, request);
        return Map.of("message", "Cap nhat diem thanh cong", "high_score", highScore);
    }
}
