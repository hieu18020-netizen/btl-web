package com.gamevui.backend.dto;

import java.util.List;

/** Tuong duong dict tra ve trong ham login() cua backend.py */
public record LoginResponse(String username, String nickname, String avatar,
                             List<String> activeChibiCodes, String token) {
}
