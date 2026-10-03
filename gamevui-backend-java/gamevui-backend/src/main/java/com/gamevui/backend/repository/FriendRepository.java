package com.gamevui.backend.repository;

import com.gamevui.backend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    /** Tim dong quan he giua 2 nguoi bat ke ai la nguoi gui truoc. */
    @Query("SELECT f FROM Friend f WHERE (f.requesterId = :a AND f.addresseeId = :b) " +
           "OR (f.requesterId = :b AND f.addresseeId = :a)")
    Optional<Friend> findPair(@Param("a") Long a, @Param("b") Long b);

    Optional<Friend> findByRequesterIdAndAddresseeId(Long requesterId, Long addresseeId);

    void deleteByRequesterIdAndAddresseeId(Long requesterId, Long addresseeId);

    /** Danh sach ban be da chap nhan (ca 2 chieu), kem thong tin hien thi cua ban. */
    @Query(value =
            "SELECT u.public_id, ISNULL(u.nickname, u.username) AS display_name, " +
            "       (ISNULL(u.high_score,0)+ISNULL(u.chess_score,0)) AS score, u.avatar " +
            "FROM friends f " +
            "JOIN users u ON u.id = CASE WHEN f.requester_id = :myId THEN f.addressee_id ELSE f.requester_id END " +
            "WHERE (f.requester_id = :myId OR f.addressee_id = :myId) AND f.status = 'accepted' " +
            "ORDER BY ISNULL(u.nickname, u.username)", nativeQuery = true)
    List<Object[]> findAcceptedFriends(@Param("myId") Long myId);

    /** Loi moi nguoi khac gui cho minh, dang cho phan hoi. */
    @Query(value =
            "SELECT u.public_id, ISNULL(u.nickname, u.username) AS display_name, " +
            "       (ISNULL(u.high_score,0)+ISNULL(u.chess_score,0)) AS score, u.avatar " +
            "FROM friends f JOIN users u ON u.id = f.requester_id " +
            "WHERE f.addressee_id = :myId AND f.status = 'pending' " +
            "ORDER BY f.created_at DESC", nativeQuery = true)
    List<Object[]> findIncomingRequests(@Param("myId") Long myId);

    /** Loi moi minh da gui, dang cho doi phuong phan hoi. */
    @Query(value =
            "SELECT u.public_id, ISNULL(u.nickname, u.username) AS display_name, " +
            "       (ISNULL(u.high_score,0)+ISNULL(u.chess_score,0)) AS score, u.avatar " +
            "FROM friends f JOIN users u ON u.id = f.addressee_id " +
            "WHERE f.requester_id = :myId AND f.status = 'pending' " +
            "ORDER BY f.created_at DESC", nativeQuery = true)
    List<Object[]> findOutgoingRequests(@Param("myId") Long myId);
}
