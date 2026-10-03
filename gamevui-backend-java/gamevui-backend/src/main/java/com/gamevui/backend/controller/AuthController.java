package com.gamevui.backend.controller;

import com.gamevui.backend.dto.LoginResponse;
import com.gamevui.backend.dto.UserLoginRequest;
import com.gamevui.backend.dto.UserRegisterRequest;
import com.gamevui.backend.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Tuong duong @app.post("/api/register") */
    @PostMapping("/api/register")
    public Map<String, String> register(@RequestBody UserRegisterRequest request) {
        authService.register(request);
        return Map.of("message", "Dang ky thanh cong");
    }

    /** Tuong duong @app.post("/api/login") */
    @PostMapping("/api/login")
    public LoginResponse login(@RequestBody UserLoginRequest request) {
        return authService.login(request);
    }
}
