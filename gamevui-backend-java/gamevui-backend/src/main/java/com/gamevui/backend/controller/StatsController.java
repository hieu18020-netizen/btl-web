package com.gamevui.backend.controller;

import com.gamevui.backend.security.OptionalUsername;
import com.gamevui.backend.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** Tuong duong @app.get("/api/user-stats/{username}") */
    @GetMapping("/api/user-stats/{username}")
    public Map<String, Object> getUserStats(@PathVariable String username) {
        return statsService.getUserStats(username);
    }

    /** Tuong duong @app.get("/api/leaderboard") */
    @GetMapping("/api/leaderboard")
    public List<Map<String, Object>> getLeaderboard(@OptionalUsername String currentUsername) {
        return statsService.getLeaderboard(currentUsername);
    }

    /** Tuong duong @app.get("/api/search-users") */
    @GetMapping("/api/search-users")
    public List<Map<String, Object>> searchUsers(@RequestParam(defaultValue = "") String q) {
        return statsService.searchUsers(q);
    }

    /** Tuong duong @app.get("/api/profile/{public_id}") */
    @GetMapping("/api/profile/{publicId}")
    public Map<String, Object> getProfile(@PathVariable String publicId, @OptionalUsername String currentUsername) {
        return statsService.getProfileByPublicId(publicId, currentUsername);
    }
}
