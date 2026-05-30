package com.chehartemple.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    private boolean emailVerified = false;
    @Column(columnDefinition = "boolean default false")
    private boolean googleUser = false;
    private String verificationToken;
    private String otp;
    private LocalDateTime otpExpiry;
    private LocalDateTime lastOtpSentAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "int default 0")
    private int failedLoginAttempts = 0;
    private LocalDateTime accountLockedUntil;

    public enum Role { USER, ADMIN }
}
