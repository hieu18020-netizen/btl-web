package com.gamevui.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tuong duong ONLINE_MESSAGE_SOCKETS (dict[str, list[WebSocket]]) + _push_to_user()
 * trong backend.py. username -> danh sach WebSocketSession dang mo (1 nguoi co the
 * mo nhieu tab/thiet bi cung luc).
 */
@Component
public class OnlineMessageSocketRegistry {

    private final Map<String, List<WebSocketSession>> sockets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public OnlineMessageSocketRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(String username, WebSocketSession session) {
        sockets.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    public void unregister(String username, WebSocketSession session) {
        List<WebSocketSession> conns = sockets.get(username);
        if (conns != null) {
            conns.remove(session);
            if (conns.isEmpty()) {
                sockets.remove(username);
            }
        }
    }

    /** Day 1 su kien realtime (tin nhan moi...) toi tat ca ket noi dang mo cua 1 nguoi dung. */
    public void pushToUser(String username, Map<String, Object> payload) {
        List<WebSocketSession> conns = sockets.get(username);
        if (conns == null || conns.isEmpty()) {
            return; // nguoi do khong online, tin nhan van nam an toan trong CSDL
        }
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession ws : conns) {
                try {
                    if (ws.isOpen()) {
                        ws.sendMessage(message);
                    }
                } catch (IOException ignored) {
                    // ket noi loi, se duoc don don khi disconnect handler chay
                }
            }
        } catch (IOException e) {
            // loi serialize JSON - khong nen xay ra voi Map don gian
        }
    }
}
