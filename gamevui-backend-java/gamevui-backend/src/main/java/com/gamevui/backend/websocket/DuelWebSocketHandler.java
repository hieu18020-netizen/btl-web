package com.gamevui.backend.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

/**
 * Tuong duong @app.websocket("/api/ws/duel/{room_code}") trong backend.py.
 * Server chi dong vai tro "relay" (chuyen tiep) - khong tinh toan vat ly/damage/luat co.
 * Moi may tu chay game cua minh roi gui trang thai cho may con lai qua dung room_code.
 */
public class DuelWebSocketHandler extends TextWebSocketHandler {

    private static final String ROOM_CODE_ATTR = "roomCode";

    private final DuelRoomRegistry registry;

    public DuelWebSocketHandler(DuelRoomRegistry registry) {
        this.registry = registry;
    }

    /** Lay room_code tu path /api/ws/duel/{room_code}, gioi han 12 ky tu, viet hoa - giong ban goc. */
    private String extractRoomCode(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        String[] parts = path.split("/");
        String raw = parts.length > 0 ? parts[parts.length - 1] : "";
        raw = raw.strip().toUpperCase();
        return raw.length() > 12 ? raw.substring(0, 12) : raw;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomCode = extractRoomCode(session);
        session.getAttributes().put(ROOM_CODE_ATTR, roomCode);

        List<WebSocketSession> room = registry.roomSessions(roomCode);

        if (room.size() >= 2) {
            // Phong da du 2 nguoi, tu choi nguoi thu 3.
            session.sendMessage(new TextMessage("{\"type\":\"room_full\"}"));
            session.close();
            return;
        }

        room.add(session);
        int role = room.size(); // 1 = nguoi tao phong, 2 = nguoi vao sau
        session.sendMessage(new TextMessage("{\"type\":\"role\",\"role\":" + role + "}"));

        if (room.size() == 2) {
            // Du nguoi roi -> an phong khoi sanh cho, khong cho nguoi thu 3 thay nua.
            registry.lobbyRooms().remove(roomCode);
            for (WebSocketSession ws : room) {
                try {
                    ws.sendMessage(new TextMessage("{\"type\":\"start\"}"));
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomCode = (String) session.getAttributes().get(ROOM_CODE_ATTR);
        List<WebSocketSession> room = registry.duelRooms().get(roomCode);
        if (room == null) {
            return;
        }
        // Chuyen tiep nguyen van tin nhan (di chuyen, don danh, mau...) cho nguoi con lai.
        for (WebSocketSession ws : room) {
            if (!ws.getId().equals(session.getId())) {
                try {
                    if (ws.isOpen()) {
                        ws.sendMessage(message);
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomCode = (String) session.getAttributes().get(ROOM_CODE_ATTR);
        if (roomCode == null) {
            return;
        }
        List<WebSocketSession> room = registry.duelRooms().get(roomCode);
        if (room != null) {
            room.remove(session);
            for (WebSocketSession ws : room) {
                try {
                    if (ws.isOpen()) {
                        ws.sendMessage(new TextMessage("{\"type\":\"opponent_left\"}"));
                    }
                } catch (IOException ignored) {
                }
            }
            if (room.isEmpty()) {
                registry.duelRooms().remove(roomCode);
            }
        }
        // Nguoi tao roi di truoc khi co ai vao -> phong khong con hop le, go khoi sanh cho.
        registry.lobbyRooms().remove(roomCode);
    }
}
