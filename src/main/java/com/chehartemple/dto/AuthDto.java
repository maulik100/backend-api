package com.chehartemple.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDto {

    @Data
    public static class SignupRequest {
        @NotBlank private String name;
        @NotBlank @Email private String email;
        @NotBlank private String password;
        private String mobile;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email private String email;
        @NotBlank private String password;
        private DeviceInfo deviceInfo;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank private String refreshToken;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String sessionToken;
        private String name;
        private String email;
        private String role;
        private long expiresIn;

        public AuthResponse(String accessToken, String refreshToken, String sessionToken, String name, String email, String role) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.sessionToken = sessionToken;
            this.name = name;
            this.email = email;
            this.role = role;
            this.expiresIn = 3600;
        }
    }

    @Data
    public static class TokenResponse {
        private String accessToken;
        private long expiresIn;

        public TokenResponse(String accessToken) {
            this.accessToken = accessToken;
            this.expiresIn = 3600;
        }
    }
}
