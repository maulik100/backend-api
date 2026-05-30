package com.chehartemple.repository;

import com.chehartemple.model.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findBySessionTokenAndStatus(String sessionToken, UserSession.SessionStatus status);
    List<UserSession> findByUserIdOrderByLoginAtDesc(Long userId);
    List<UserSession> findByUserIdAndStatus(Long userId, UserSession.SessionStatus status);
    List<UserSession> findByStatus(UserSession.SessionStatus status);

    Page<UserSession> findByLoginAtAfterOrderByLoginAtDesc(LocalDateTime after, Pageable pageable);
    Page<UserSession> findByLoginSourceNotOrderByLoginAtDesc(String source, Pageable pageable);
    Page<UserSession> findByLoginAtAfterAndLoginSourceNotOrderByLoginAtDesc(LocalDateTime after, String source, Pageable pageable);

    // Count unique users (not sessions) - exclude ADMIN source
    @Query("SELECT COUNT(DISTINCT s.userId) FROM UserSession s WHERE s.loginAt >= :after AND s.loginSource <> 'ADMIN'")
    long countUniqueUsersAfter(@Param("after") LocalDateTime after);

    // Count active sessions excluding admin
    @Query("SELECT COUNT(DISTINCT s.userId) FROM UserSession s WHERE s.status = 'ACTIVE' AND s.loginSource <> 'ADMIN'")
    long countUniqueActiveUsers();

    // Active sessions list excluding admin
    @Query("SELECT s FROM UserSession s WHERE s.status = 'ACTIVE' AND s.loginSource <> 'ADMIN' ORDER BY s.loginAt DESC")
    List<UserSession> findActiveNonAdminSessions();

    // Daily unique user logins (not session count) - exclude admin
    @Query("SELECT CAST(s.loginAt AS date) as day, COUNT(DISTINCT s.userId) FROM UserSession s WHERE s.loginAt >= :from AND s.loginSource <> 'ADMIN' GROUP BY CAST(s.loginAt AS date) ORDER BY day")
    List<Object[]> countUniqueUsersByDay(@Param("from") LocalDateTime from);

    // Login source breakdown (unique users) - exclude admin
    @Query("SELECT s.loginSource, COUNT(DISTINCT s.userId) FROM UserSession s WHERE s.loginAt >= :from AND s.loginSource <> 'ADMIN' GROUP BY s.loginSource")
    List<Object[]> countUniqueUsersBySource(@Param("from") LocalDateTime from);

    // Total sessions count for period (for reference)
    long countByLoginAtAfter(LocalDateTime after);
    long countByStatus(UserSession.SessionStatus status);

    // Search (exclude admin)
    @Query("SELECT s FROM UserSession s WHERE s.loginSource <> 'ADMIN' AND (LOWER(s.email) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(s.deviceModel) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(s.loginSource) LIKE LOWER(CONCAT('%',:q,'%'))) ORDER BY s.loginAt DESC")
    Page<UserSession> searchSessions(@Param("q") String query, Pageable pageable);

    @Query("SELECT s FROM UserSession s WHERE s.loginSource <> 'ADMIN' AND s.loginAt >= :after AND (LOWER(s.email) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(s.deviceModel) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(s.loginSource) LIKE LOWER(CONCAT('%',:q,'%'))) ORDER BY s.loginAt DESC")
    Page<UserSession> searchSessionsAfter(@Param("q") String query, @Param("after") LocalDateTime after, Pageable pageable);
}
