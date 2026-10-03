package com.gamevui.backend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tuong duong 2 dict toan cuc trong backend.py:
 *   DUEL_ROOMS: dict[str, list[WebSocket]]   -> phong dang choi (WebSocket relay)
 *   LOBBY_ROOMS: dict[str, dict]             -> sanh cho (phong chua du nguoi)
 * cung ham _generate_room_code().
 *
 * Gop chung vao 1 registry vi generateRoomCode() can kiem tra khong trung ma
 * o CA HAI danh sach, giong ham goc.
 */
@Component
public class DuelRoomRegistry {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // bo ky tu de nham (0/O, 1/I...)
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, LobbyRoomInfo> lobbyRooms = new ConcurrentHashMap<>();
    private final Map<String, List<WebSocketSession>> duelRooms = new ConcurrentHashMap<>();

    public Map<String, LobbyRoomInfo> lobbyRooms() {
        return lobbyRooms;
    }

    public Map<String, List<WebSocketSession>> duelRooms() {
        return duelRooms;
    }

    /** Tuong duong _generate_room_code() */
    public String generateRoomCode() {
        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
            String code = sb.toString();
            if (!duelRooms.containsKey(code) && !lobbyRooms.containsKey(code)) {
                return code;
            }
        }
    }

    /** Tuong duong _purge_stale_lobby_rooms() */
    public void purgeStaleLobbyRooms(long ttlSeconds) {
        long now = System.currentTimeMillis();
        lobbyRooms.entrySet().removeIf(e -> (now - e.getValue().getCreatedAtMillis()) > ttlSeconds * 1000);
    }

    /** Lay (hoac tao moi) danh sach WebSocket cua 1 phong dau, dung khi co nguoi vao phong. */
    public List<WebSocketSession> roomSessions(String roomCode) {
        return duelRooms.computeIfAbsent(roomCode, k -> new CopyOnWriteArrayList<>());
    }
}
