package com.gamevui.backend.repository;

import com.gamevui.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByPublicId(String publicId);

    boolean existsByPublicId(String publicId);

    /**
     * Tuong duong truy van thu hang trong get_user_stats / get_profile_by_public_id:
     *   SELECT rank_num FROM (
     *       SELECT username, ROW_NUMBER() OVER (ORDER BY (high_score + chess_score) DESC, id ASC) as rank_num
     *       FROM users
     *   ) as ranked_users WHERE username = ?
     */
    @Query(value =
            "SELECT rank_num FROM ( " +
            "   SELECT username, ROW_NUMBER() OVER (ORDER BY (ISNULL(high_score,0) + ISNULL(chess_score,0)) DESC, id ASC) AS rank_num " +
            "   FROM users " +
            ") ranked WHERE username = :username", nativeQuery = true)
    Optional<Integer> findRankByUsername(@Param("username") String username);

    @Query(value =
            "SELECT rank_num FROM ( " +
            "   SELECT public_id, ROW_NUMBER() OVER (ORDER BY (ISNULL(high_score,0) + ISNULL(chess_score,0)) DESC, id ASC) AS rank_num " +
            "   FROM users " +
            ") ranked WHERE public_id = :publicId", nativeQuery = true)
    Optional<Integer> findRankByPublicId(@Param("publicId") String publicId);

    /**
     * Top 10 bang xep hang, tuong duong /api/leaderboard.
     */
    @Query(value =
            "SELECT TOP 10 username, ISNULL(nickname, username) AS display_name, " +
            "       (ISNULL(high_score,0) + ISNULL(chess_score,0)) AS score, avatar, public_id " +
            "FROM users ORDER BY score DESC, id ASC", nativeQuery = true)
    List<Object[]> findLeaderboardTop10();

    /**
     * Tim kiem theo bi danh (nickname/username) hoac public_id, tuong duong /api/search-users.
     */
    @Query(value =
            "SELECT TOP 8 ISNULL(nickname, username) AS display_name, " +
            "       (ISNULL(high_score,0) + ISNULL(chess_score,0)) AS score, avatar, public_id " +
            "FROM users " +
            "WHERE ISNULL(nickname, username) LIKE :pattern OR public_id LIKE :pattern " +
            "ORDER BY score DESC, id ASC", nativeQuery = true)
    List<Object[]> searchUsers(@Param("pattern") String pattern);
}
