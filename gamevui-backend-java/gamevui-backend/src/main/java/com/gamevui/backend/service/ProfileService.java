package com.gamevui.backend.service;

import com.gamevui.backend.dto.SetActiveChibiRequest;
import com.gamevui.backend.dto.UpdateAvatarRequest;
import com.gamevui.backend.dto.UpdateNicknameRequest;
import com.gamevui.backend.entity.User;
import com.gamevui.backend.repository.UserRepository;
import com.gamevui.backend.security.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Tuong duong 3 endpoint /api/update-nickname, /api/update-avatar, /api/set-active-chibi.
 */
@Service
public class ProfileService {

    // Chi chap nhan dung dinh dang data URL anh: data:image/<loai>;base64,<du lieu base64>
    private static final Pattern AVATAR_DATA_URL_RE =
            Pattern.compile("^data:image/(png|jpe?g|gif|webp);base64,[A-Za-z0-9+/=]+$");

    // Ma chibi chi gom chu/so, toi da 30 ky tu.
    private static final Pattern CHIBI_CODE_RE = Pattern.compile("^[A-Za-z0-9_-]{1,30}$");

    private static final int MAX_ACTIVE_CHIBIS = 8;

    private final UserRepository userRepository;
    private final int maxAvatarLength;

    public ProfileService(UserRepository userRepository,
                           @Value("${app.avatar.max-length}") int maxAvatarLength) {
        this.userRepository = userRepository;
        this.maxAvatarLength = maxAvatarLength;
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Khong tim thay nguoi dung"));
    }

    @Transactional
    public void updateNickname(String username, UpdateNicknameRequest req) {
        User user = requireUser(username);
        user.setNickname(req.nickname());
        userRepository.save(user);
    }

    @Transactional
    public void updateAvatar(String username, UpdateAvatarRequest req) {
        String avatar = req.avatar();
        if (avatar != null && avatar.length() > maxAvatarLength) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Anh qua lon, vui long chon anh nho hon");
        }
        if (avatar != null && !avatar.isEmpty() && !AVATAR_DATA_URL_RE.matcher(avatar).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Dinh dang anh dai dien khong hop le");
        }
        User user = requireUser(username);
        user.setAvatar(avatar);
        userRepository.save(user);
    }

    @Transactional
    public List<String> setActiveChibi(String username, SetActiveChibiRequest req) {
        String code = req.chibiCode() == null ? "" : req.chibiCode().strip();
        if (code.isEmpty() || !CHIBI_CODE_RE.matcher(code).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ma chibi khong hop le");
        }

        User user = requireUser(username);
        List<String> codes = ChibiCodec.parse(user.getActiveChibiCode());

        if (req.activeOrDefault()) {
            if (!codes.contains(code)) {
                if (codes.size() >= MAX_ACTIVE_CHIBIS) {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            "Da dat so luong chibi toi da co the chay cung luc");
                }
                codes.add(code);
            }
        } else {
            codes.remove(code);
        }

        user.setActiveChibiCode(ChibiCodec.serialize(codes));
        userRepository.save(user);
        return codes;
    }
}
