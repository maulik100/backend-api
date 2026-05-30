package com.chehartemple.service;

import com.chehartemple.model.AuditLog;
import com.chehartemple.model.User;
import com.chehartemple.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // For authenticated user actions (admin CRUD, user activity tracking)
    public void log(String action, String module, String entityId, String description, String source, String sessionToken, HttpServletRequest request) {
        User user = getCurrentUser();
        if (user == null) return;

        AuditLog log = AuditLog.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .action(action)
                .module(module)
                .entityId(entityId)
                .description(description)
                .ipAddress(getIp(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .sessionToken(sessionToken)
                .source(source)
                .build();
        auditLogRepository.save(log);
    }

    // Shortcut without sessionToken (for admin CRUD where session doesn't matter)
    public void log(String action, String module, String entityId, String description, String source, HttpServletRequest request) {
        log(action, module, entityId, description, source, null, request);
    }

    // With location (for mobile activity tracking)
    public void logWithLocation(String action, String module, String entityId, String description, String source, String sessionToken, String location, HttpServletRequest request) {
        User user = getCurrentUser();
        if (user == null) return;

        AuditLog log = AuditLog.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .action(action)
                .module(module)
                .entityId(entityId)
                .description(description)
                .ipAddress(getIp(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .sessionToken(sessionToken)
                .location(location)
                .source(source)
                .build();
        auditLogRepository.save(log);
    }

    // For login/logout where user is not yet in SecurityContext
    public void log(Long userId, String email, String action, String module, String description, String source, String sessionToken, String ipAddress, String userAgent) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .email(email)
                .action(action)
                .module(module)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .sessionToken(sessionToken)
                .source(source)
                .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLog> getAll(int page, int size) {
        return auditLogRepository.findByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public List<AuditLog> getByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AuditLog> getByModule(String module) {
        return auditLogRepository.findByModuleOrderByCreatedAtDesc(module);
    }

    public List<AuditLog> getBySession(String sessionToken) {
        return auditLogRepository.findBySessionTokenOrderByCreatedAtAsc(sessionToken);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    private String getIp(HttpServletRequest request) {
        if (request == null) return null;
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip : request.getRemoteAddr();
    }
}
