package com.gamevui.backend.dto;

/** Tuong duong class ChessMatchResult(BaseModel) trong backend.py: result = "win"|"loss"|"draw" */
public record ChessMatchResultRequest(String roomCode, String result) {
}
