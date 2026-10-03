package com.gamevui.backend.service;

import com.gamevui.backend.dto.SendMessageRequest;
import com.gamevui.backend.entity.Message;
import com.gamevui.backend.entity.User;
import com.gamevui.backend.repository.MessageRepository;
import com.gamevui.backend.repository.UserRepository;
import com.gamevui.backend.security.ApiException;
import com.gamevui.backend.websocket.OnlineMessageSocketRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tuong duong toan bo phan "===== Nhan tin (Messenger) =====" trong backend.py
 * (tru websocket /api/ws/messages, xem MessageWebSocketHandler).
 */
@Service
public class MessageService {

    private static final int MAX_CONTENT_LENGTH = 2000;

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FriendService friendService;
    private final OnlineMessageSocketRegistry socketRegistry;

    public MessageService(UserRepository userRepository, MessageRepository messageRepository,
                           FriendService friendService, OnlineMessageSocketRegistry socketRegistry) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.friendService = friendService;
        this.socketRegistry = socketRegistry;
    }

    /** Tuong duong /api/messages/conversations (GET) */
    public List<Map<String, Object>> listConversations(String currentUsername) {
        User me = friendService.requireUserByUsername(currentUsername);
        List<Object[]> rows = messageRepository.findConversations(me.getId());

        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("public_id", r[0]);
            m.put("display_name", r[1]);
            m.put("avatar", r[2] != null ? r[2] : "");
            m.put("last_message", r[3]);
            LocalDateTime createdAt = (LocalDateTime) r[4];
            m.put("last_message_at", createdAt != null ? createdAt.toString() : null);
            Long senderId = r[5] != null ? ((Number) r[5]).longValue() : null;
            m.put("last_message_is_mine", senderId != null ? senderId.equals(me.getId()) : null);
            m.put("unread_count", r[6] != null ? ((Number) r[6]).intValue() : 0);
            return m;
        }).toList();
    }

    /** Tuong duong /api/messages/{public_id} (GET): lich su tin nhan, phan trang bang before_id. */
    @Transactional
    public Map<String, Object> getConversation(String currentUsername, String targetPublicId,
                                                Long beforeId, int limit) {
        User me = friendService.requireUserByUsername(currentUsername);
        User target = friendService.requireUserByPublicId(targetPublicId);

        if (!FriendStatus.FRIENDS.equals(friendService.getFriendStatus(me.getId(), target.getId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Chi co the xem tin nhan voi ban be");
        }

        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Object[]> rows = messageRepository.findConversationPage(me.getId(), target.getId(), beforeId, safeLimit);

        List<Map<String, Object>> messages = rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", ((Number) r[0]).longValue());
            Long senderId = ((Number) r[1]).longValue();
            m.put("is_mine", senderId.equals(me.getId()));
            m.put("content", r[2]);
            LocalDateTime createdAt = (LocalDateTime) r[3];
            m.put("created_at", createdAt != null ? createdAt.toString() : null);
            return m;
        }).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        java.util.Collections.reverse(messages); // tra ve theo thu tu thoi gian tang dan

        messageRepository.markAsRead(target.getId(), me.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("messages", messages);
        result.put("has_more", rows.size() == safeLimit);
        return result;
    }

    /** Tuong duong /api/messages/{public_id} (POST): gui tin nhan + day realtime qua WebSocket. */
    @Transactional
    public Map<String, Object> sendMessage(String currentUsername, String targetPublicId, SendMessageRequest req) {
        String content = req.content() == null ? "" : req.content().strip();
        if (content.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Noi dung tin nhan khong duoc de trong");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tin nhan qua dai (toi da 2000 ky tu)");
        }

        User me = friendService.requireUserByUsername(currentUsername);
        User target = friendService.requireUserByPublicId(targetPublicId);

        if (target.getId().equals(me.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Khong the tu nhan tin cho chinh minh");
        }
        if (!FriendStatus.FRIENDS.equals(friendService.getFriendStatus(me.getId(), target.getId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Chi co the nhan tin voi ban be");
        }

        Message message = new Message();
        message.setSenderId(me.getId());
        message.setReceiverId(target.getId());
        message.setContent(content);
        message = messageRepository.save(message);

        String createdAtIso = message.getCreatedAt().toString();

        Map<String, Object> pushPayload = new LinkedHashMap<>();
        pushPayload.put("type", "new_message");
        pushPayload.put("public_id", me.getPublicId());
        pushPayload.put("display_name", me.getNickname() != null ? me.getNickname() : me.getUsername());
        pushPayload.put("avatar", me.getAvatar() != null ? me.getAvatar() : "");
        pushPayload.put("content", content);
        pushPayload.put("created_at", createdAtIso);
        pushPayload.put("message_id", message.getId());
        socketRegistry.pushToUser(target.getUsername(), pushPayload);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message_id", message.getId());
        result.put("created_at", createdAtIso);
        return result;
    }
}
