package com.chehartemple.repository;

import com.chehartemple.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // All (for internal use)
    Page<AuditLog> findByOrderByCreatedAtDesc(Pageable pageable);
    Page<AuditLog> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after, Pageable pageable);

    // Exclude ADMIN source (for audit trail display - only user/mobile activity)
    Page<AuditLog> findBySourceNotOrderByCreatedAtDesc(String source, Pageable pageable);
    Page<AuditLog> findByCreatedAtAfterAndSourceNotOrderByCreatedAtDesc(LocalDateTime after, String source, Pageable pageable);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AuditLog> findByModuleOrderByCreatedAtDesc(String module);
    List<AuditLog> findBySessionTokenOrderByCreatedAtAsc(String sessionToken);
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    long countByCreatedAtAfter(LocalDateTime after);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :from AND a.source <> 'ADMIN' GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countByAction(@Param("from") LocalDateTime from);

    @Query("SELECT a.module, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :from AND a.source <> 'ADMIN' GROUP BY a.module ORDER BY COUNT(a) DESC")
    List<Object[]> countByModule(@Param("from") LocalDateTime from);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt >= :after AND a.source <> 'ADMIN'")
    long countNonAdminAfter(@Param("after") LocalDateTime after);

    // Search (exclude admin)
    @Query("SELECT a FROM AuditLog a WHERE a.source <> 'ADMIN' AND (LOWER(a.email) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(a.action) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%',:q,'%'))) ORDER BY a.createdAt DESC")
    Page<AuditLog> searchLogs(@Param("q") String query, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.source <> 'ADMIN' AND a.createdAt >= :after AND (LOWER(a.email) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(a.action) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%',:q,'%'))) ORDER BY a.createdAt DESC")
    Page<AuditLog> searchLogsAfter(@Param("q") String query, @Param("after") LocalDateTime after, Pageable pageable);
}
