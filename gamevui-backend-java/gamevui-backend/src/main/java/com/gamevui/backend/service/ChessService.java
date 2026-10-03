package com.gamevui.backend.service;

import com.gamevui.backend.dto.ChessMatchResultRequest;
import com.gamevui.backend.entity.User;
import com.gamevui.backend.repository.UserRepository;
import com.gamevui.backend.security.ApiException;
import com.gamevui.backend.websocket.ChessRoomReportRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tuong duong /api/chess/report-result trong backend.py.
 *
 * Ai thang +100 diem, ai thua -100 diem (hoa thi khong doi) vao cot chess_score.
 * Moi van co hop le (thang, thua, hoac hoa) deu +1 vao total_matches cho CA 2 nguoi choi.
 *
 * Chong gian lan co ban: diem CHI duoc cong/tru khi CA HAI nguoi choi trong cung 1 phong
 * deu tu bao cao ket qua (qua API co token dang nhap) va 2 bao cao do khop nhau
 * (1 thang - 1 thua, hoac ca 2 hoa). Neu chi 1 nguoi bao hoac 2 nguoi bao mau thuan nhau
 * thi KHONG tinh diem cho ai ca.
 */
@Service
public class ChessService {

    private final UserRepository userRepository;
    private final ChessRoomReportRegistry reportRegistry;
    private final int scoreDelta;
    private final long reportTtlSeconds;

    public ChessService(UserRepository userRepository, ChessRoomReportRegistry reportRegistry,
                         @Value("${app.chess.score-delta}") int scoreDelta,
                         @Value("${app.chess.report-ttl-seconds}") long reportTtlSeconds) {
        this.userRepository = userRepository;
        this.reportRegistry = reportRegistry;
        this.scoreDelta = scoreDelta;
        this.reportTtlSeconds = reportTtlSeconds;
    }

    @Transactional
    public Map<String, Object> reportResult(String currentUsername, ChessMatchResultRequest req) {
        reportRegistry.purgeStale(reportTtlSeconds);

        String roomCode = req.roomCode() == null ? "" : req.roomCode().strip().toUpperCase();
        if (roomCode.length() > 12) {
            roomCode = roomCode.substring(0, 12);
        }
        String result = req.result() == null ? "" : req.result().strip().toLowerCase();

        if (roomCode.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Thieu ma phong");
        }
        if (!result.equals("win") && !result.equals("loss") && !result.equals("draw")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ket qua khong hop le");
        }

        Map<String, ChessRoomReportRegistry.Report> reports = reportRegistry.reportsFor(roomCode);
        reports.put(currentUsername, new ChessRoomReportRegistry.Report(result, System.currentTimeMillis()));

        // Chua du 2 nguoi bao cao -> chi ghi nhan, cho doi thu bao cao not.
        if (reports.size() < 2) {
            return Map.of("message", "Da ghi nhan, dang cho xac nhan tu doi thu", "applied", false);
        }

        String[] usernames = reports.keySet().toArray(new String[0]);
        String r1 = reports.get(usernames[0]).result();
        String r2 = reports.get(usernames[1]).result();
        boolean consistent = (r1.equals("win") && r2.equals("loss"))
                || (r1.equals("loss") && r2.equals("win"))
                || (r1.equals("draw") && r2.equals("draw"));

        // Dung 1 lan roi xoa, tranh bao cao lai nhieu lan de cong diem khong.
        Map<String, ChessRoomReportRegistry.Report> finalReports = new LinkedHashMap<>(reports);
        reportRegistry.remove(roomCode);

        if (!consistent) {
            return Map.of("message", "Ket qua 2 nguoi choi bao cao khong khop nhau, khong tinh diem", "applied", false);
        }

        for (Map.Entry<String, ChessRoomReportRegistry.Report> entry : finalReports.entrySet()) {
            User user = userRepository.findByUsername(entry.getKey()).orElse(null);
            if (user == null) {
                continue;
            }
            int delta = switch (entry.getValue().result()) {
                case "win" -> scoreDelta;
                case "loss" -> -scoreDelta;
                default -> 0;
            };
            user.setChessScore(user.getChessScore() + delta);
            user.setTotalMatches(user.getTotalMatches() + 1);
            userRepository.save(user);
        }

        int myDelta = switch (result) {
            case "win" -> scoreDelta;
            case "loss" -> -scoreDelta;
            default -> 0;
        };

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Da cap nhat diem co vua");
        response.put("applied", true);
        response.put("delta", myDelta);
        return response;
    }
}
