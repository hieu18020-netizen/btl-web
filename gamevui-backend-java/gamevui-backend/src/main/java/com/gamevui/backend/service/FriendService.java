package com.gamevui.backend.service;

import com.gamevui.backend.dto.FriendRespondRequest;
import com.gamevui.backend.entity.Friend;
import com.gamevui.backend.entity.User;
import com.gamevui.backend.repository.FriendRepository;
import com.gamevui.backend.repository.UserRepository;
import com.gamevui.backend.security.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tuong duong toan bo phan "===== Ket ban =====" trong backend.py:
 * cac ham _get_user_id / _get_user_by_public_id / _get_friend_status
 * va 4 endpoint /api/friends/request, /respond, DELETE /api/friends, /api/friends (GET).
 */
@Service
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public FriendService(UserRepository userRepository, FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    public User requireUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));
    }

    public User requireUserByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi choi"));
    }

    /** Tuong duong _get_friend_status(cursor, my_id, target_id) */
    public String getFriendStatus(Long myId, Long targetId) {
        if (myId.equals(targetId)) {
            return FriendStatus.SELF;
        }
        Optional<Friend> found = friendRepository.findPair(myId, targetId);
        if (found.isEmpty()) {
            return FriendStatus.NONE;
        }
        Friend f = found.get();
        if (Friend.STATUS_ACCEPTED.equals(f.getStatus())) {
            return FriendStatus.FRIENDS;
        }
        return f.getRequesterId().equals(myId) ? FriendStatus.PENDING_SENT : FriendStatus.PENDING_RECEIVED;
    }

    @Transactional
    public Map<String, String> sendRequest(String currentUsername, String targetPublicId) {
        User me = requireUserByUsername(currentUsername);
        User target = requireUserByPublicId(targetPublicId);

        if (target.getId().equals(me.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Khong the tu ket ban voi chinh minh");
        }

        Optional<Friend> existing = friendRepository.findPair(me.getId(), target.getId());
        if (existing.isPresent()) {
            Friend f = existing.get();
            if (Friend.STATUS_ACCEPTED.equals(f.getStatus())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Hai nguoi da la ban be");
            }
            if (f.getRequesterId().equals(me.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Ban da gui loi moi ket ban truoc do");
            }
            // Doi phuong da gui loi moi cho minh truoc do -> chap nhan luon, khong tao dong moi.
            f.setStatus(Friend.STATUS_ACCEPTED);
            friendRepository.save(f);
            return Map.of("message", "Da chap nhan loi moi ket ban", "friend_status", FriendStatus.FRIENDS);
        }

        Friend f = new Friend();
        f.setRequesterId(me.getId());
        f.setAddresseeId(target.getId());
        f.setStatus(Friend.STATUS_PENDING);
        friendRepository.save(f);
        return Map.of("message", "Da gui loi moi ket ban", "friend_status", FriendStatus.PENDING_SENT);
    }

    @Transactional
    public Map<String, String> respondRequest(String currentUsername, String targetPublicId, FriendRespondRequest req) {
        String action = req.action() == null ? "" : req.action().strip().toLowerCase();
        if (!action.equals("accept") && !action.equals("decline")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Hanh dong khong hop le");
        }

        User me = requireUserByUsername(currentUsername);
        User target = requireUserByPublicId(targetPublicId);

        Friend f = friendRepository.findByRequesterIdAndAddresseeId(target.getId(), me.getId())
                .filter(x -> Friend.STATUS_PENDING.equals(x.getStatus()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong co loi moi ket ban nao tu nguoi nay"));

        if (action.equals("accept")) {
            f.setStatus(Friend.STATUS_ACCEPTED);
            friendRepository.save(f);
            return Map.of("message", "Da chap nhan ket ban", "friend_status", FriendStatus.FRIENDS);
        } else {
            friendRepository.delete(f);
            return Map.of("message", "Da tu choi loi moi ket ban", "friend_status", FriendStatus.NONE);
        }
    }

    @Transactional
    public Map<String, String> removeFriend(String currentUsername, String targetPublicId) {
        User me = requireUserByUsername(currentUsername);
        User target = requireUserByPublicId(targetPublicId);

        friendRepository.findPair(me.getId(), target.getId()).ifPresent(friendRepository::delete);
        return Map.of("message", "Da huy ket ban", "friend_status", FriendStatus.NONE);
    }

    private List<Map<String, Object>> toDisplayList(List<Object[]> rows) {
        return rows.stream().map(r -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("public_id", r[0]);
            m.put("display_name", r[1]);
            m.put("high_score", r[2]);
            m.put("avatar", r[3] != null ? r[3] : "");
            return m;
        }).toList();
    }

    public Map<String, Object> listFriends(String currentUsername) {
        User me = requireUserByUsername(currentUsername);
        Long myId = me.getId();
        return Map.of(
                "friends", toDisplayList(friendRepository.findAcceptedFriends(myId)),
                "incoming", toDisplayList(friendRepository.findIncomingRequests(myId)),
                "outgoing", toDisplayList(friendRepository.findOutgoingRequests(myId))
        );
    }
}
