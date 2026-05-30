package com.chehartemple.service;

import com.chehartemple.dto.AuthDto.*;
import com.chehartemple.dto.DeviceInfo;
import com.chehartemple.exception.ApiException;
import com.chehartemple.model.User;
import com.chehartemple.model.UserSession;
import com.chehartemple.repository.UserRepository;
import com.chehartemple.repository.UserSessionRepository;
import com.chehartemple.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.core.io.ClassPathResource;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;
    private final UserSessionService userSessionService;
    private final AuditService auditService;
    private final EmailValidator emailValidator;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public String signup(SignupRequest req) {
        // Validate email is not disposable
        emailValidator.validate(req.getEmail());

        // Validate password strength
        validatePassword(req.getPassword());

        if (userRepository.existsByEmail(req.getEmail())) {
            throw ApiException.conflict("An account with this email already exists. Please login or use a different email.");
        }
        String otp = generateOtp();
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .mobile(req.getMobile())
                .role(User.Role.USER)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .otp(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(10))
                .lastOtpSentAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        sendOtpEmail(user.getEmail(), user.getName(), otp);
        return "Registration successful. Please check your email for OTP to verify your account.";
    }

    public String verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("No account found with this email."));
        if (user.isEmailVerified()) {
            return "Email is already verified. You can login.";
        }
        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            throw ApiException.badRequest("No OTP found. Please request a new one.");
        }
        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw ApiException.badRequest("OTP has expired. Please request a new one.");
        }
        if (!user.getOtp().equals(otp)) {
            throw ApiException.badRequest("Invalid OTP. Please try again.");
        }
        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        user.setVerificationToken(null);
        userRepository.save(user);
        return "Email verified successfully. You can now login.";
    }

    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("No account found with this email."));
        if (user.isEmailVerified()) {
            throw ApiException.badRequest("Email is already verified.");
        }
        if (user.getLastOtpSentAt() != null && user.getLastOtpSentAt().plusMinutes(1).isAfter(LocalDateTime.now())) {
            throw ApiException.badRequest("Please wait 1 minute before requesting a new OTP.");
        }
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        user.setLastOtpSentAt(LocalDateTime.now());
        userRepository.save(user);
        sendOtpEmail(user.getEmail(), user.getName(), otp);
        return "OTP sent successfully. Please check your email.";
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw ApiException.badRequest("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw ApiException.badRequest("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw ApiException.badRequest("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw ApiException.badRequest("Password must contain at least one digit.");
        }
    }

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 30;

    public AuthResponse login(LoginRequest req, String ipAddress, String userAgent, String source) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("No account found with this email. Please sign up first."));

        // Check if account is locked
        if (user.getAccountLockedUntil() != null && LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
            long minutesLeft = java.time.Duration.between(LocalDateTime.now(), user.getAccountLockedUntil()).toMinutes() + 1;
            throw ApiException.forbidden("Your account is locked due to multiple failed login attempts. Please try again after " + minutesLeft + " minutes.");
        }

        // If lock period has passed, reset attempts
        if (user.getAccountLockedUntil() != null && LocalDateTime.now().isAfter(user.getAccountLockedUntil())) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                userRepository.save(user);
                throw ApiException.forbidden("Your account has been locked for " + LOCK_DURATION_MINUTES + " minutes due to " + MAX_FAILED_ATTEMPTS + " failed login attempts. Please try again later.");
            }
            userRepository.save(user);
            int remaining = MAX_FAILED_ATTEMPTS - attempts;
            throw ApiException.unauthorized("Incorrect password. You have " + remaining + " attempt(s) remaining before your account is locked.");
        }

        // Successful login — reset failed attempts
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
        }

        if (!user.isEmailVerified()) {
            throw ApiException.forbidden("Your email is not verified. Please check your inbox and verify your email before logging in.");
        }

        // Determine actual login source based on role + platform
        String actualSource = resolveSource(user, source);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        String sessionToken = userSessionService.createSession(user, req.getDeviceInfo(), ipAddress, actualSource);

        // Audit log with sessionToken
        auditService.log(user.getId(), user.getEmail(), "LOGIN", "AUTH",
                "Login from " + actualSource, actualSource, sessionToken, ipAddress, userAgent);

        return new AuthResponse(accessToken, refreshToken, sessionToken, user.getName(), user.getEmail(), user.getRole().name());
    }

    public void logout(String sessionToken, String ipAddress, String userAgent, String source) {
        userSessionRepository.findBySessionTokenAndStatus(sessionToken, UserSession.SessionStatus.ACTIVE)
                .ifPresent(session -> {
                    session.setLogoutAt(LocalDateTime.now());
                    session.setStatus(UserSession.SessionStatus.LOGGED_OUT);
                    userSessionRepository.save(session);
                    // Audit log with sessionToken
                    auditService.log(session.getUserId(), session.getEmail(), "LOGOUT", "AUTH",
                            "Logout from " + session.getLoginSource(), session.getLoginSource(), sessionToken, ipAddress, userAgent);
                });
    }

    /**
     * Resolve login source:
     * - ADMIN role = "ADMIN"
     * - USER role + MOBILE_APP header = "MOBILE"
     * - USER role + browser = "USER"
     */
    private String resolveSource(User user, String detectedSource) {
        if (user.getRole() == User.Role.ADMIN) return "ADMIN";
        if ("MOBILE_APP".equals(detectedSource)) return "MOBILE";
        return "USER";
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw ApiException.unauthorized("Your session has expired. Please login again.");
        }
        String email = jwtUtil.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("Account not found. Please sign up again."));
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        return new TokenResponse(newAccessToken);
    }

    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired verification link. Please request a new one."));
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return "Email verified successfully. You can now login.";
    }

    private void sendOtpEmail(String email, String name, String otp) {
        try {
            String html = loadTemplate("templates/otp-email.html")
                    .replace("{{NAME}}", name)
                    .replace("{{OTP}}", otp)
                    .replace("{{EXPIRY_MINUTES}}", "10");

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail, "Chehar Temple");
            helper.setTo(email);
            helper.setSubject("Chehar Temple - Email Verification OTP");
            helper.setText(html, true);
            helper.addInline("templeLogo", new ClassPathResource("templates/chehar-maa-small.png"));
            mailSender.send(mimeMessage);
            log.info("OTP email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage(), e);
            throw ApiException.serverError("Failed to send verification email. Please try again later.");
        }
    }

    private String loadTemplate(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IOException("Template not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw ApiException.serverError("Email template not found.");
        }
    }
}
