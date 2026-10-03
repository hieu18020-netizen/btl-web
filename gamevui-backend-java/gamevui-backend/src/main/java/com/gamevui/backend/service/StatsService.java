package com.gamevui.backend.service;

import com.gamevui.backend.entity.User;
import com.gamevui.backend.repository.UserRepository;
import com.gamevui.backend.security.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tuong duong /api/user-stats/{username}, /api/leaderboard, /api/search-users,
 * /api/profile/{public_id} trong backend.py.
 */
@Service
public class StatsService {

    private final UserRepository userRepository;
    private final FriendService friendService;
    private final PublicIdService publicIdService;

    public StatsService(UserRepository userRepository, FriendService friendService,
                         PublicIdService publicIdService) {
        this.userRepository = userRepository;
        this.friendService = friendService;
        this.publicIdService = publicIdService;
    }

    @Transactional
    public Map<String, Object> getUserStats(String username) {
        User user = userRepository.findByUsername(username).orElse(null);

        int rank = userRepository.findRankByUsername(username).orElse(0);
        int totalMatches = user != null ? user.getTotalMatches() : 0;
        int highScore = user != null ? user.getTotalScore() : 0;
        String avatar = user != null && user.getAvatar() != null ? user.getAvatar() : "";
        String publicId = user != null ? user.getPublicId() : null;
        List<String> activeChibiCodes = user != null ? ChibiCodec.parse(user.getActiveChibiCode()) : List.of();

        // Tai khoan cu (tao truoc khi co tinh nang public_id) se co gia tri null o day.
        // Tu sinh 1 ma moi va luu lai vao CSDL luon.
        if (user != null && (publicId == null || publicId.isEmpty())) {
            publicId = publicIdService.generate();
            user.setPublicId(publicId);
            userRepository.save(user);
        }
        if (publicId == null) {
            publicId = "--------";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rank", "#" + rank);
        result.put("total_matches", totalMatches);
        result.put("high_score", highScore);
        result.put("avatar", avatar);
        result.put("public_id", publicId);
        result.put("active_chibi_codes", activeChibiCodes);
        return result;
    }

    public List<Map<String, Object>> getLeaderboard(String currentUsername) {
        List<Object[]> rows = userRepository.findLeaderboardTop10();
        List<Map<String, Object>> leaderboard = new java.util.ArrayList<>();
        int rank = 1;
        for (Object[] r : rows) {
            String rowUsername = (String) r[0];
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank", rank++);
            m.put("username", r[1]);
            m.put("score", r[2]);
            m.put("avatar", r[3] != null ? r[3] : "");
            m.put("public_id", r[4] != null ? r[4] : "--------");
            // Khong tra ten dang nhap that cho ai xem cung thay nua.
            // Chi bao true/false cho dung chu tai khoan, dua vao token cua chinh nguoi goi API.
            m.put("is_me", currentUsername != null && rowUsername.equalsIgnoreCase(currentUsername));
            leaderboard.add(m);
        }
        return leaderboard;
    }

    public List<Map<String, Object>> searchUsers(String q) {
        String query = q == null ? "" : q.strip();
        if (query.isEmpty()) {
            return List.of();
        }
        List<Object[]> rows = userRepository.searchUsers("%" + query + "%");
        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("display_name", r[0]);
            m.put("high_score", r[1]);
            m.put("avatar", r[2] != null ? r[2] : "");
            m.put("public_id", r[3] != null ? r[3] : "--------");
            return m;
        }).toList();
    }

    public Map<String, Object> getProfileByPublicId(String publicId, String currentUsername) {
        User target = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi choi"));

        int rank = userRepository.findRankByPublicId(publicId).orElse(0);

        // Chi tinh quan he ket ban khi nguoi xem da dang nhap; khach vang lai xem ho so
        // binh thuong, khong thay trang thai ket ban (frontend se an nut ket ban).
        String friendStatus = null;
        if (currentUsername != null) {
            User me = userRepository.findByUsername(currentUsername).orElse(null);
            if (me != null) {
                friendStatus = friendService.getFriendStatus(me.getId(), target.getId());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("display_name", target.getNickname() != null ? target.getNickname() : target.getUsername());
        result.put("high_score", target.getTotalScore());
        result.put("avatar", target.getAvatar() != null ? target.getAvatar() : "");
        result.put("public_id", target.getPublicId());
        result.put("rank", "#" + rank);
        result.put("total_matches", target.getTotalMatches());
        result.put("friend_status", friendStatus);
        return result;
    }
}
