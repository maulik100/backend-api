package com.chehartemple.service;

import com.chehartemple.dto.AuthDto.AuthResponse;
import com.chehartemple.dto.DeviceInfo;
import com.chehartemple.exception.ApiException;
import com.chehartemple.model.User;
import com.chehartemple.repository.UserRepository;
import com.chehartemple.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserSessionService userSessionService;
    private final ConfigService configService;
    private final EmailValidator emailValidator;

    public AuthResponse loginWithGoogle(String idToken, DeviceInfo deviceInfo, String ipAddress, String userAgent, String source) {
        Map<String, Object> payload = verifyGoogleToken(idToken);
        if (payload == null) {
            throw ApiException.unauthorized("Google sign-in failed. Token verification failed.");
        }

        String email = (String) payload.get("email");
        String name = payload.containsKey("name") ? (String) payload.get("name") : null;
        if (name == null && payload.containsKey("given_name")) {
            name = (String) payload.get("given_name");
        }

        if (email == null || email.isEmpty()) {
            throw ApiException.badRequest("Unable to get email from Google.");
        }

        // Block disposable emails even via Google
        emailValidator.validate(email);

        final String finalName = name;

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .name(finalName != null ? finalName : email.split("@")[0])
                    .email(email)
                    .password("GOOGLE_AUTH") // Placeholder for Google users
                    .role(User.Role.USER)
                    .emailVerified(true)
                    .googleUser(true)
                    .build();
            log.info("Creating new Google user: {}", email);
            return userRepository.save(newUser);
        });

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Resolve actual source: ADMIN role=ADMIN, USER+mobile=MOBILE, USER+browser=USER
        String actualSource = resolveSource(user, source);
        String sessionToken = userSessionService.createSession(user, deviceInfo, ipAddress, actualSource);

        return new AuthResponse(accessToken, refreshToken, sessionToken, user.getName(), user.getEmail(), user.getRole().name());
    }

    private String resolveSource(User user, String detectedSource) {
        if (user.getRole() == User.Role.ADMIN) return "ADMIN";
        if ("MOBILE_APP".equals(detectedSource)) return "MOBILE";
        return "USER";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> verifyGoogleToken(String idToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> response = responseEntity.getBody();

            if (response == null) {
                log.error("Google tokeninfo returned null body");
                return null;
            }

            String aud = (String) response.get("aud");
            log.info("Google token verified - aud: {}, email: {}", aud, response.get("email"));

            String googleClientId = configService.getGoogleClientId();
            String googleAndroidClientId = configService.getGoogleAndroidClientId();

            // Accept token from Web client or Android client
            if (googleClientId.equals(aud)) {
                return response;
            }
            if (googleAndroidClientId != null && !googleAndroidClientId.isEmpty() && googleAndroidClientId.equals(aud)) {
                return response;
            }

            log.error("Audience mismatch. Config client-id: [{}], android-client-id: [{}], Token aud: [{}]", googleClientId, googleAndroidClientId, aud);
            return null;

        } catch (HttpClientErrorException e) {
            log.error("Google tokeninfo HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Google token verification exception: {}", e.getMessage(), e);
        }
        return null;
    }
}
