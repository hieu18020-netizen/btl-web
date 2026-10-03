package com.gamevui.backend.service;

import com.gamevui.backend.dto.GameScoreRequest;
import com.gamevui.backend.entity.User;
import com.gamevui.backend.repository.UserRepository;
import com.gamevui.backend.security.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tuong duong ACTIVE_GAME_SESSIONS (dict trong RAM) + /api/start-game + /api/update-score
 * + _cleanup_expired_sessions() trong backend.py.
 *
 * Luu y: giong ban goc, day la state trong bo nho cua 1 instance duy nhat.
 * Neu scale nhieu instance server thi can chuyen sang Redis (ngoai pham vi do an nay).
 */
@Service
public class GameSessionService {

    private record Session(String username, long startTimeMillis) {
    }

    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final long sessionTtlSeconds;
    private final int minPlaySeconds;
    private final int maxScorePerSecond;

    public GameSessionService(UserRepository userRepository,
                               @Value("${app.game.session-ttl-seconds}") long sessionTtlSeconds,
                               @Value("${app.game.min-play-seconds}") int minPlaySeconds,
                               @Value("${app.game.max-score-per-second}") int maxScorePerSecond) {
        this.userRepository = userRepository;
        this.sessionTtlSeconds = sessionTtlSeconds;
        this.minPlaySeconds = minPlaySeconds;
        this.maxScorePerSecond = maxScorePerSecond;
    }

    /** Don rac phien het han, chay dinh ky moi 5 phut (tuong duong _cleanup_expired_sessions goi thu cong). */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        activeSessions.entrySet().removeIf(e ->
                (now - e.getValue().startTimeMillis()) > sessionTtlSeconds * 1000);
    }

    /** Tuong duong /api/start-game: tao session_token ngau nhien, luu thoi diem bat dau. */
    public String startGame(String username) {
        cleanupExpiredSessions();
        String token = randomHex(32);
        activeSessions.put(token, new Session(username, System.currentTimeMillis()));
        return token;
    }

    private static String randomHex(int chars) {
        StringBuilder sb = new StringBuilder();
        String hexDigits = "0123456789abcdef";
        for (int i = 0; i < chars; i++) {
            sb.append(hexDigits.charAt(RANDOM.nextInt(16)));
        }
        return sb.toString();
    }

    /** Tuong duong /api/update-score: kiem tra session hop le + chong gian lan roi cap nhat high_score. */
    @Transactional
    public int updateScore(String username, GameScoreRequest req) {
        Session session = activeSessions.remove(req.sessionToken());
        if (session == null || !session.username().equals(username)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Phien choi khong hop le hoac da het han, vui long choi lai tu dau");
        }

        double elapsedSeconds = (System.currentTimeMillis() - session.startTimeMillis()) / 1000.0;
        if (elapsedSeconds < minPlaySeconds) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ket qua khong hop le");
        }

        double maxAllowedScore = elapsedSeconds * maxScorePerSecond;
        if (req.score() < 0 || req.score() > maxAllowedScore) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Diem so khong hop le");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));

        int newHigh = Math.max(user.getHighScore(), req.score());
        user.setHighScore(newHigh);
        user.setTotalMatches(user.getTotalMatches() + 1);
        userRepository.save(user);
        return newHigh;
    }
}
