package com.chehartemple.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String action; // LOGIN, LOGOUT, CREATE_EVENT, UPDATE_EVENT, DELETE_EVENT, etc.

    @Column(nullable = false)
    private String module; // AUTH, EVENT, NEWS, GALLERY, TIMING, CONFIG, USER

    private String entityId; // ID of affected entity
    private String description;
    private String ipAddress;
    private String userAgent;
    private String sessionToken; // Links activity to a login session
    private String location; // User's location (city, state, country)

    @Column(nullable = false)
    private String source; // ADMIN, USER, MOBILE

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
