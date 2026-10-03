package com.gamevui.backend.controller;

import com.gamevui.backend.dto.FriendRespondRequest;
import com.gamevui.backend.security.CurrentUsername;
import com.gamevui.backend.service.FriendService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /** Tuong duong @app.post("/api/friends/request/{public_id}") */
    @PostMapping("/api/friends/request/{publicId}")
    public Map<String, String> sendRequest(@PathVariable String publicId, @CurrentUsername String username) {
        return friendService.sendRequest(username, publicId);
    }

    /** Tuong duong @app.post("/api/friends/respond/{public_id}") */
    @PostMapping("/api/friends/respond/{publicId}")
    public Map<String, String> respondRequest(@PathVariable String publicId,
                                                @RequestBody FriendRespondRequest request,
                                                @CurrentUsername String username) {
        return friendService.respondRequest(username, publicId, request);
    }

    /** Tuong duong @app.delete("/api/friends/{public_id}") */
    @DeleteMapping("/api/friends/{publicId}")
    public Map<String, String> removeFriend(@PathVariable String publicId, @CurrentUsername String username) {
        return friendService.removeFriend(username, publicId);
    }

    /** Tuong duong @app.get("/api/friends") */
    @GetMapping("/api/friends")
    public Map<String, Object> listFriends(@CurrentUsername String username) {
        return friendService.listFriends(username);
    }
}
