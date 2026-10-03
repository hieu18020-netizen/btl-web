package com.gamevui.backend.websocket;

import com.gamevui.backend.security.JwtUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Tuong duong @app.websocket("/api/ws/messages") trong backend.py.
 * Trinh duyet (WebSocket API) khong tu set duoc header Authorization, nen token
 * duoc truyen qua query string: /api/ws/messages?token=<jwt>.
 */
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private static final String USERNAME_ATTR = "username";

    private final JwtUtil jwtUtil;
    private final OnlineMessageSocketRegistry registry;

    public MessageWebSocketHandler(JwtUtil jwtUtil, OnlineMessageSocketRegistry registry) {
        this.jwtUtil = jwtUtil;
        this.registry = registry;
    }

    private String extractToken(WebSocketSession session) {
        List<String> tokens = UriComponentsBuilder.fromUri(session.getUri()).build()
                .getQueryParams().get("token");
        return (tokens == null || tokens.isEmpty()) ? "" : tokens.get(0);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractToken(session);
        JwtUtil.DecodeResult result = jwtUtil.decode(token);

        if (!(result instanceof JwtUtil.Ok ok)) {
            session.sendMessage(new TextMessage("{\"type\":\"auth_error\"}"));
            session.close();
            return;
        }

        session.getAttributes().put(USERNAME_ATTR, ok.username());
        registry.register(ok.username(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Khong can xu ly du lieu tu client - kenh nay chi dung de server day tin xuong.
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = (String) session.getAttributes().get(USERNAME_ATTR);
        if (username != null) {
            registry.unregister(username, session);
        }
    }
}
