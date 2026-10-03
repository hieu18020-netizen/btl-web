package com.gamevui.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Anh xa bang "messages":
 *
 *   CREATE TABLE messages (
 *       id INT IDENTITY(1,1) PRIMARY KEY,
 *       sender_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
 *       receiver_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
 *       content NVARCHAR(2000) NOT NULL,
 *       created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
 *       is_read BIT NOT NULL DEFAULT 0
 *   );
 *   CREATE INDEX IX_messages_conversation ON messages(sender_id, receiver_id, created_at);
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public Message() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
