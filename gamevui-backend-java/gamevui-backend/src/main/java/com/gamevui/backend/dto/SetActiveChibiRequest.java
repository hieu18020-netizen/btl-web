package com.gamevui.backend.dto;

/** Tuong duong class SetActiveChibi(BaseModel) trong backend.py */
public record SetActiveChibiRequest(String chibiCode, Boolean active) {
    public boolean activeOrDefault() {
        return active == null || active;
    }
}
