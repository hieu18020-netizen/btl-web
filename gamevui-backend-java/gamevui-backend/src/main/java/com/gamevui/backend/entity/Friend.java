package com.gamevui.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Anh xa bang "friends":
 *
 *   CREATE TABLE friends (
 *       id INT IDENTITY(1,1) PRIMARY KEY,
 *       requester_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
 *       addressee_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
 *       status VARCHAR(10) NOT NULL DEFAULT 'pending',
 *       created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
 *       CONSTRAINT UQ_friend_pair UNIQUE (requester_id, addressee_id)
 *   );
 */
@Entity
@Table(name = "friends")
public class Friend {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "accepted";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "addressee_id", nullable = false)
    private Long addresseeId;

    @Column(nullable = false, length = 10)
    private String status = STATUS_PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Friend() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }

    public Long getAddresseeId() { return addresseeId; }
    public void setAddresseeId(Long addresseeId) { this.addresseeId = addresseeId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
