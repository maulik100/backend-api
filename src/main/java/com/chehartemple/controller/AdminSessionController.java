package com.chehartemple.controller;

import com.chehartemple.model.AuditLog;
import com.chehartemple.model.UserSession;
import com.chehartemple.repository.AuditLogRepository;
import com.chehartemple.repository.UserSessionRepository;
import com.chehartemple.service.AuditService;
import com.chehartemple.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSessionController {

    private final UserSessionService userSessionService;
    private final UserSessionRepository userSessionRepository;
    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;

    // ==================== STATS ====================

    @GetMapping("/audit-stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam(defaultValue = "7") int days) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime periodStart = LocalDateTime.now().minusDays(days);

        Map<String, Object> stats = new LinkedHashMap<>();

        // Unique active users (currently logged in, excluding admin)
        long activeUsers = userSessionRepository.countUniqueActiveUsers();
        stats.put("activeUsers", activeUsers);

        // Today's unique user logins (excluding admin)
        long todayUniqueUsers = userSessionRepository.countUniqueUsersAfter(todayStart);
        stats.put("todayLogins", todayUniqueUsers);

        // Period unique user logins (excluding admin)
        long periodUniqueUsers = userSessionRepository.countUniqueUsersAfter(periodStart);
        stats.put("periodLogins", periodUniqueUsers);

        // Total audit actions in period (excluding admin)
        long periodActions = auditLogRepository.countNonAdminAfter(periodStart);
        stats.put("periodActions", periodActions);

        // Daily unique user login chart (excluding admin)
        List<Object[]> dailyCounts = userSessionRepository.countUniqueUsersByDay(periodStart);
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date.toString());
            point.put("count", 0L);
            chartData.add(point);
        }
        for (Object[] row : dailyCounts) {
            String dayStr = row[0].toString();
            long count = (Long) row[1];
            for (Map<String, Object> point : chartData) {
                if (point.get("date").toString().equals(dayStr)) {
                    point.put("count", count);
                    break;
                }
            }
        }
        stats.put("dailyLogins", chartData);

        // Login by source breakdown (unique users, excluding admin)
        List<Object[]> sourceCounts = userSessionRepository.countUniqueUsersBySource(periodStart);
        Map<String, Long> sourceMap = new LinkedHashMap<>();
        for (Object[] row : sourceCounts) {
            sourceMap.put(row[0] != null ? row[0].toString() : "UNKNOWN", (Long) row[1]);
        }
        stats.put("loginsBySource", sourceMap);

        // Top actions breakdown
        List<Object[]> actionCounts = auditLogRepository.countByAction(periodStart);
        Map<String, Long> actionMap = new LinkedHashMap<>();
        for (Object[] row : actionCounts) {
            actionMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("topActions", actionMap);

        // Module breakdown
        List<Object[]> moduleCounts = auditLogRepository.countByModule(periodStart);
        Map<String, Long> moduleMap = new LinkedHashMap<>();
        for (Object[] row : moduleCounts) {
            moduleMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("moduleBreakdown", moduleMap);

        // Active user sessions list (excluding admin)
        List<UserSession> activeSessions = userSessionRepository.findActiveNonAdminSessions();
        stats.put("activeSessionsList", activeSessions);

        return ResponseEntity.ok(stats);
    }

    // ==================== SESSIONS ====================

    @GetMapping("/sessions")
    public ResponseEntity<Page<UserSession>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int days,
            @RequestParam(defaultValue = "") String search) {
        PageRequest pr = PageRequest.of(page, size);
        if (!search.isEmpty() && days > 0) {
            return ResponseEntity.ok(userSessionRepository.searchSessionsAfter(search, LocalDateTime.now().minusDays(days), pr));
        } else if (!search.isEmpty()) {
            return ResponseEntity.ok(userSessionRepository.searchSessions(search, pr));
        } else if (days > 0) {
            return ResponseEntity.ok(userSessionRepository.findByLoginAtAfterAndLoginSourceNotOrderByLoginAtDesc(
                    LocalDateTime.now().minusDays(days), "ADMIN", pr));
        }
        return ResponseEntity.ok(userSessionRepository.findByLoginSourceNotOrderByLoginAtDesc("ADMIN", pr));
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<List<UserSession>> getActiveSessions() {
        return ResponseEntity.ok(userSessionService.getActiveSessions());
    }

    @GetMapping("/sessions/user/{userId}")
    public ResponseEntity<List<UserSession>> getUserSessions(@PathVariable Long userId) {
        return ResponseEntity.ok(userSessionService.getUserSessions(userId));
    }

    // ==================== AUDIT LOGS ====================

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int days,
            @RequestParam(defaultValue = "") String search) {
        PageRequest pr = PageRequest.of(page, size);
        if (!search.isEmpty() && days > 0) {
            return ResponseEntity.ok(auditLogRepository.searchLogsAfter(search, LocalDateTime.now().minusDays(days), pr));
        } else if (!search.isEmpty()) {
            return ResponseEntity.ok(auditLogRepository.searchLogs(search, pr));
        } else if (days > 0) {
            return ResponseEntity.ok(auditLogRepository.findByCreatedAtAfterAndSourceNotOrderByCreatedAtDesc(
                    LocalDateTime.now().minusDays(days), "ADMIN", pr));
        }
        return ResponseEntity.ok(auditLogRepository.findBySourceNotOrderByCreatedAtDesc("ADMIN", pr));
    }

    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.getByUser(userId));
    }

    @GetMapping("/audit-logs/module/{module}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByModule(@PathVariable String module) {
        return ResponseEntity.ok(auditService.getByModule(module));
    }

    // Session journey: all activities for a specific session (login → screens → logout)
    @GetMapping("/audit-logs/session/{sessionToken}")
    public ResponseEntity<List<AuditLog>> getSessionJourney(@PathVariable String sessionToken) {
        return ResponseEntity.ok(auditService.getBySession(sessionToken));
    }
}
