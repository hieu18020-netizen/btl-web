package com.gamevui.backend.service;

import com.gamevui.backend.repository.UserRepository;
import com.gamevui.backend.security.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/** Tuong duong generate_public_id(cursor) trong backend.py */
@Service
public class PublicIdService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 20;

    private final UserRepository userRepository;

    public PublicIdService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Sinh 1 ma so 8 chu so ngau nhien, dam bao khong trung voi ai da co trong bang users. */
    public String generate() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String candidate = String.format("%08d", RANDOM.nextInt(100_000_000));
            if (!userRepository.existsByPublicId(candidate)) {
                return candidate;
            }
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Khong the tao ma dinh danh, vui long thu lai");
    }
}
