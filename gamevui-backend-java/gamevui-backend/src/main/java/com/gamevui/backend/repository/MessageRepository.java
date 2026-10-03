package com.gamevui.backend.repository;

import com.gamevui.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Danh sach hoi thoai = moi ban be, kem tin nhan gan nhat (OUTER APPLY) va so tin chua doc.
     * Tuong duong truy van trong list_conversations().
     */
    @Query(value =
            "SELECT u.public_id, ISNULL(u.nickname, u.username) AS display_name, u.avatar, " +
            "       lm.content, lm.created_at, lm.sender_id, " +
            "       ISNULL((SELECT COUNT(*) FROM messages um " +
            "               WHERE um.sender_id = u.id AND um.receiver_id = :myId AND um.is_read = 0), 0) AS unread " +
            "FROM friends f " +
            "JOIN users u ON u.id = CASE WHEN f.requester_id = :myId THEN f.addressee_id ELSE f.requester_id END " +
            "OUTER APPLY ( " +
            "    SELECT TOP 1 content, created_at, sender_id FROM messages m " +
            "    WHERE (m.sender_id = u.id AND m.receiver_id = :myId) OR (m.sender_id = :myId AND m.receiver_id = u.id) " +
            "    ORDER BY m.created_at DESC " +
            ") lm " +
            "WHERE (f.requester_id = :myId OR f.addressee_id = :myId) AND f.status = 'accepted' " +
            "ORDER BY ISNULL(lm.created_at, CAST('1900-01-01' AS DATETIME2)) DESC", nativeQuery = true)
    List<Object[]> findConversations(@Param("myId") Long myId);

    /**
     * Lich su tin nhan giua 2 nguoi, moi nhat truoc, phan trang bang before_id.
     * (beforeId = null nghia la lay tu tin moi nhat)
     */
    @Query(value =
            "SELECT TOP (:limit) id, sender_id, content, created_at FROM messages " +
            "WHERE ((sender_id = :myId AND receiver_id = :targetId) OR (sender_id = :targetId AND receiver_id = :myId)) " +
            "  AND (:beforeId IS NULL OR id < :beforeId) " +
            "ORDER BY id DESC", nativeQuery = true)
    List<Object[]> findConversationPage(@Param("myId") Long myId, @Param("targetId") Long targetId,
                                         @Param("beforeId") Long beforeId, @Param("limit") int limit);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
    int markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
