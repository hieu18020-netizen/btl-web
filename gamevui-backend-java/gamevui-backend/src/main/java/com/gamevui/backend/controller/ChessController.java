package com.gamevui.backend.controller;

import com.gamevui.backend.dto.ChessMatchResultRequest;
import com.gamevui.backend.security.CurrentUsername;
import com.gamevui.backend.service.ChessService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChessController {

    private final ChessService chessService;

    public ChessController(ChessService chessService) {
        this.chessService = chessService;
    }

    /** Tuong duong @app.post("/api/chess/report-result") */
    @PostMapping("/api/chess/report-result")
    public Map<String, Object> reportResult(@RequestBody ChessMatchResultRequest request,
                                              @CurrentUsername String username) {
        return chessService.reportResult(username, request);
    }
}
