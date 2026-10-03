package com.gamevui.backend.dto;

/** Tuong duong class GameScore(BaseModel) trong backend.py */
public record GameScoreRequest(int score, String sessionToken) {
}
