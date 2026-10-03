package com.gamevui.backend.entity;

import jakarta.persistence.*;

/**
 * Anh xa bang "users" (da ton tai san trong CSDL, xem cac cot duoc dung
 * trong toan bo backend.py: username, password, nickname, avatar, high_score,
 * total_matches, public_id, active_chibi_code, chess_score).
 *
 * ddl-auto=none nen entity nay CHI mo ta lai cau truc bang co san, khong tao bang moi.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nickname;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String avatar; // chuoi base64 data URL

    @Column(name = "high_score")
    private Integer highScore = 0;

    @Column(name = "chess_score")
    private Integer chessScore = 0;

    @Column(name = "total_matches")
    private Integer totalMatches = 0;

    @Column(name = "public_id", length = 8)
    private String publicId;

    // Nhieu ma chibi cach nhau boi dau phay, vd "1,2" (xem parseActiveChibis / serializeActiveChibis)
    @Column(name = "active_chibi_code")
    private String activeChibiCode;

    public User() {
    }

    // ===== Getters & setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Integer getHighScore() { return highScore == null ? 0 : highScore; }
    public void setHighScore(Integer highScore) { this.highScore = highScore; }

    public Integer getChessScore() { return chessScore == null ? 0 : chessScore; }
    public void setChessScore(Integer chessScore) { this.chessScore = chessScore; }

    public Integer getTotalMatches() { return totalMatches == null ? 0 : totalMatches; }
    public void setTotalMatches(Integer totalMatches) { this.totalMatches = totalMatches; }

    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getActiveChibiCode() { return activeChibiCode; }
    public void setActiveChibiCode(String activeChibiCode) { this.activeChibiCode = activeChibiCode; }

    /** Tong diem thanh tich = high_score (Tetris) + chess_score (Co vua), dung de xep hang. */
    @Transient
    public int getTotalScore() {
        return getHighScore() + getChessScore();
    }
}
