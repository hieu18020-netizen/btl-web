package com.gamevui.backend.controller;

import com.gamevui.backend.dto.SetActiveChibiRequest;
import com.gamevui.backend.dto.UpdateAvatarRequest;
import com.gamevui.backend.dto.UpdateNicknameRequest;
import com.gamevui.backend.security.CurrentUsername;
import com.gamevui.backend.service.ProfileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /** Tuong duong @app.post("/api/update-nickname") */
    @PostMapping("/api/update-nickname")
    public Map<String, String> updateNickname(@RequestBody UpdateNicknameRequest request,
                                                @CurrentUsername String username) {
        profileService.updateNickname(username, request);
        return Map.of("message", "Cap nhat bi danh thanh cong");
    }

    /** Tuong duong @app.post("/api/update-avatar") */
    @PostMapping("/api/update-avatar")
    public Map<String, String> updateAvatar(@RequestBody UpdateAvatarRequest request,
                                              @CurrentUsername String username) {
        profileService.updateAvatar(username, request);
        return Map.of("message", "Cap nhat anh dai dien thanh cong");
    }

    /** Tuong duong @app.post("/api/set-active-chibi") */
    @PostMapping("/api/set-active-chibi")
    public Map<String, List<String>> setActiveChibi(@RequestBody SetActiveChibiRequest request,
                                                       @CurrentUsername String username) {
        List<String> codes = profileService.setActiveChibi(username, request);
        return Map.of("active_chibi_codes", codes);
    }
}
