package com.gamevui.backend.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tuong duong CHESS_ROOM_REPORTS: dict[str, dict] trong backend.py.
 * room_code -> {username: {"result": str, "ts": float}}
 */
@Component
public class ChessRoomReportRegistry {

    public record Report(String result, long timestampMillis) {
    }

    private final Map<String, Map<String, Report>> rooms = new ConcurrentHashMap<>();

    public Map<String, Report> reportsFor(String roomCode) {
        return rooms.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>());
    }

    public void remove(String roomCode) {
        rooms.remove(roomCode);
    }

    /** Tuong duong _purge_stale_chess_reports() */
    public void purgeStale(long ttlSeconds) {
        long now = System.currentTimeMillis();
        rooms.entrySet().removeIf(entry ->
                entry.getValue().values().stream()
                        .allMatch(r -> (now - r.timestampMillis()) > ttlSeconds * 1000));
    }
}
