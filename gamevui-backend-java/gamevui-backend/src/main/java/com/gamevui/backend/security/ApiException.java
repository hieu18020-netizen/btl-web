package com.gamevui.backend.security;

import org.springframework.http.HttpStatus;

/**
 * Tuong duong raise HTTPException(status_code=..., detail=...) trong Python.
 * Duoc GlobalExceptionHandler bat va tra ve JSON {"detail": message} giong FastAPI mac dinh.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String detail) {
        super(detail);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
