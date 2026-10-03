package com.gamevui.backend.config;

import com.gamevui.backend.security.JwtUtil;
import com.gamevui.backend.websocket.DuelRoomRegistry;
import com.gamevui.backend.websocket.DuelWebSocketHandler;
import com.gamevui.backend.websocket.MessageWebSocketHandler;
import com.gamevui.backend.websocket.OnlineMessageSocketRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Dang ky 2 endpoint WebSocket, tuong duong:
 *   @app.websocket("/api/ws/messages")
 *   @app.websocket("/api/ws/duel/{room_code}")
 * trong backend.py.
 *
 * setAllowedOriginPatterns("*") tuong duong CORSMiddleware allow_origins=["*"] cho HTTP.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtUtil jwtUtil;
    private final OnlineMessageSocketRegistry onlineMessageSocketRegistry;
    private final DuelRoomRegistry duelRoomRegistry;

    public WebSocketConfig(JwtUtil jwtUtil, OnlineMessageSocketRegistry onlineMessageSocketRegistry,
                            DuelRoomRegistry duelRoomRegistry) {
        this.jwtUtil = jwtUtil;
        this.onlineMessageSocketRegistry = onlineMessageSocketRegistry;
        this.duelRoomRegistry = duelRoomRegistry;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MessageWebSocketHandler(jwtUtil, onlineMessageSocketRegistry), "/api/ws/messages")
                .setAllowedOriginPatterns("*");

        // "/**" o cuoi de bat duoc bat ky room_code nao, giong {room_code} trong FastAPI.
        registry.addHandler(new DuelWebSocketHandler(duelRoomRegistry), "/api/ws/duel/**")
                .setAllowedOriginPatterns("*");
    }
}
