package com.gamevui.backend.controller;

import com.gamevui.backend.dto.SendMessageRequest;
import com.gamevui.backend.security.CurrentUsername;
import com.gamevui.backend.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** Tuong duong @app.get("/api/messages/conversations") */
    @GetMapping("/api/messages/conversations")
    public List<Map<String, Object>> listConversations(@CurrentUsername String username) {
        return messageService.listConversations(username);
    }

    /** Tuong duong @app.get("/api/messages/{public_id}") */
    @GetMapping("/api/messages/{publicId}")
    public Map<String, Object> getConversation(@PathVariable String publicId,
                                                 @RequestParam(name = "before_id", required = false) Long beforeId,
                                                 @RequestParam(defaultValue = "50") int limit,
                                                 @CurrentUsername String username) {
        return messageService.getConversation(username, publicId, beforeId, limit);
    }

    /** Tuong duong @app.post("/api/messages/{public_id}") */
    @PostMapping("/api/messages/{publicId}")
    public Map<String, Object> sendMessage(@PathVariable String publicId,
                                             @RequestBody SendMessageRequest request,
                                             @CurrentUsername String username) {
        return messageService.sendMessage(username, publicId, request);
    }
}
