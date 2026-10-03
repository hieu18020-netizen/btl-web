package com.gamevui.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Tuong duong 3 ham trong backend.py:
 *   create_token(username), get_current_username(...), get_optional_username(...)
 *
 * SECRET_KEY / ALGORITHM (HS256) / TOKEN_EXPIRE_HOURS duoc cau hinh trong application.properties
 * (app.jwt.secret, app.jwt.expiration-hours).
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationHours;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.expiration-hours}") long expirationHours) {
        // HS256 can key >= 256 bit; secret cau hinh nen la 1 chuoi du dai.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = expirationHours;
    }

    /** Tuong duong create_token(username) - payload {"sub": username, "exp": ...} */
    public String createToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    /** Ket qua giai ma: chua username hoac bao loi cu the (het han / khong hop le). */
    public sealed interface DecodeResult permits Ok, Expired, Invalid {}
    public record Ok(String username) implements DecodeResult {}
    public record Expired() implements DecodeResult {}
    public record Invalid() implements DecodeResult {}

    public DecodeResult decode(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            return new Ok(claims.getSubject());
        } catch (ExpiredJwtException e) {
            return new Expired();
        } catch (JwtException | IllegalArgumentException e) {
            return new Invalid();
        }
    }
}
