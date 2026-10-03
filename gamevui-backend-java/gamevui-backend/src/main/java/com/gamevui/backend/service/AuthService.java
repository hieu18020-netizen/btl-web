package com.gamevui.backend.service;

import com.gamevui.backend.dto.LoginResponse;
import com.gamevui.backend.dto.UserLoginRequest;
import com.gamevui.backend.dto.UserRegisterRequest;
import com.gamevui.backend.entity.User;
import com.gamevui.backend.repository.UserRepository;
import com.gamevui.backend.security.ApiException;
import com.gamevui.backend.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/**
 * Tuong duong 2 endpoint /api/register va /api/login trong backend.py.
 */
@Service
public class AuthService {

    // Chi cho phep tai khoan/mat khau khong chua khoang trang; mat khau bat buoc co ca chu va so.
    private static final Pattern USERNAME_RE = Pattern.compile("^\\S{8,}$");
    private static final Pattern PASSWORD_RE = Pattern.compile("^(?=\\S{8,}$)(?=.*[A-Za-z])(?=.*\\d)\\S+$");

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final PublicIdService publicIdService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordService passwordService,
                        PublicIdService publicIdService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.publicIdService = publicIdService;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void register(UserRegisterRequest req) {
        if (req.username() == null || !USERNAME_RE.matcher(req.username()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Ten tai khoan khong duoc chua khoang trang va phai co it nhat 8 ky tu");
        }
        if (req.password() == null || !PASSWORD_RE.matcher(req.password()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Mat khau khong duoc chua khoang trang, toi thieu 8 ky tu va phai co ca chu va so");
        }

        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ten tai khoan da ton tai");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordService.hash(req.password()));
        user.setPublicId(publicIdService.generate());
        // Tai khoan moi mac dinh dung luon chibi "1" (Eruka) ngay lan dau dang nhap.
        user.setActiveChibiCode("1");
        user.setHighScore(0);
        user.setTotalMatches(0);
        user.setChessScore(0);
        userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(UserLoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Ten dang nhap hoac mat khau khong dung"));

        String stored = user.getPassword() == null ? "" : user.getPassword();
        boolean isHashed = passwordService.isHashed(stored);

        boolean passwordOk = isHashed
                ? passwordService.verify(req.password(), stored)
                : stored.equals(req.password()); // tai khoan cu, mat khau con luu tho

        if (!passwordOk) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Ten dang nhap hoac mat khau khong dung");
        }

        if (!isHashed) {
            // Nang cap ngay len hash de khong con luu mat khau tho nua.
            user.setPassword(passwordService.hash(req.password()));
            userRepository.save(user);
        }

        String token = jwtUtil.createToken(user.getUsername());
        return new LoginResponse(
                user.getUsername(),
                user.getNickname() == null ? "" : user.getNickname(),
                user.getAvatar() == null ? "" : user.getAvatar(),
                ChibiCodec.parse(user.getActiveChibiCode()),
                token
        );
    }
}
