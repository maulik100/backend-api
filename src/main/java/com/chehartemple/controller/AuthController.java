package com.chehartemple.controller;

import com.chehartemple.dto.AuthDto.*;
import com.chehartemple.dto.DeviceInfo;
import com.chehartemple.exception.ApiException;
import com.chehartemple.service.AuthService;
import com.chehartemple.service.GoogleAuthService;
import com.chehartemple.service.LoginRateLimiter;
import com.chehartemple.service.SignupRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final SignupRateLimiter signupRateLimiter;
    private final LoginRateLimiter loginRateLimiter;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request, HttpServletRequest httpRequest) {
        signupRateLimiter.checkRateLimit(getIp(httpRequest));
        return ResponseEntity.ok(Map.of("message", authService.signup(request)));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        if (email == null || otp == null) {
            throw ApiException.badRequest("Email and OTP are required.");
        }
        return ResponseEntity.ok(Map.of("message", authService.verifyOtp(email, otp)));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null) {
            throw ApiException.badRequest("Email is required.");
        }
        return ResponseEntity.ok(Map.of("message", authService.resendOtp(email)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getIp(httpRequest);
        loginRateLimiter.checkRateLimit(ip);
        String userAgent = httpRequest.getHeader("User-Agent");
        String source = detectSource(httpRequest);
        return ResponseEntity.ok(authService.login(request, ip, userAgent, source));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> body, HttpServletRequest httpRequest) {
        String sessionToken = body.get("sessionToken");
        if (sessionToken != null) {
            String ip = getIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            String source = detectSource(httpRequest);
            authService.logout(sessionToken, ip, userAgent, source);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody Map<String, Object> body, HttpServletRequest httpRequest) {
        String idToken = (String) body.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            throw ApiException.badRequest("Google ID token is required.");
        }
        String ip = getIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String source = detectSource(httpRequest);

        DeviceInfo deviceInfo = null;
        if (body.containsKey("deviceInfo") && body.get("deviceInfo") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> d = (Map<String, String>) body.get("deviceInfo");
            deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceId(d.get("deviceId"));
            deviceInfo.setDeviceName(d.get("deviceName"));
            deviceInfo.setDeviceModel(d.get("deviceModel"));
            deviceInfo.setOsName(d.get("osName"));
            deviceInfo.setOsVersion(d.get("osVersion"));
            deviceInfo.setAppVersion(d.get("appVersion"));
        }
        return ResponseEntity.ok(googleAuthService.loginWithGoogle(idToken, deviceInfo, ip, userAgent, source));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request.getRefreshToken()));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("message", authService.verifyEmail(token)));
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip : request.getRemoteAddr();
    }

    private String detectSource(HttpServletRequest request) {
        // Client sends X-Source header: ADMIN, USER, MOBILE_APP
        String source = request.getHeader("X-Source");
        if (source != null) return source;
        String ua = request.getHeader("User-Agent");
        if (ua != null && ua.contains("Android")) return "MOBILE_APP";
        return "USER";
    }
}
