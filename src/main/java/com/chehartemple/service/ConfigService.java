package com.chehartemple.service;

import com.chehartemple.model.AppConfig;
import com.chehartemple.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final AppConfigRepository appConfigRepository;

    @Value("${app.google.client-id:}")
    private String defaultGoogleClientId;

    @Value("${app.google.client-secret:}")
    private String defaultGoogleClientSecret;

    @Value("${app.google.android-client-id:}")
    private String defaultGoogleAndroidClientId;

    @Value("${app.facebook.app-id:}")
    private String defaultFbAppId;

    @Value("${app.facebook.app-secret:}")
    private String defaultFbAppSecret;

    @Value("${app.facebook.page-access-token:}")
    private String defaultFbPageAccessToken;

    @Value("${app.facebook.page-id:}")
    private String defaultFbPageId;

    @Value("${app.instagram.access-token:}")
    private String defaultIgAccessToken;

    @Value("${app.instagram.user-id:}")
    private String defaultIgUserId;

    public String get(String key, String fallback) {
        return appConfigRepository.findByConfigKey(key)
                .map(AppConfig::getConfigValue)
                .filter(v -> v != null && !v.isEmpty())
                .orElse(fallback);
    }

    public String getGoogleClientId() {
        return get("GOOGLE_CLIENT_ID", defaultGoogleClientId);
    }

    public String getGoogleClientSecret() {
        return get("GOOGLE_CLIENT_SECRET", defaultGoogleClientSecret);
    }

    public String getGoogleAndroidClientId() {
        return get("GOOGLE_ANDROID_CLIENT_ID", defaultGoogleAndroidClientId);
    }

    public String getFbAppId() {
        return get("FB_APP_ID", defaultFbAppId);
    }

    public String getFbAppSecret() {
        return get("FB_APP_SECRET", defaultFbAppSecret);
    }

    public String getFbPageAccessToken() {
        return get("FB_ACCESS_TOKEN", defaultFbPageAccessToken);
    }

    public String getFbPageId() {
        return get("FB_PAGE_ID", defaultFbPageId);
    }

    public String getIgAccessToken() {
        return get("IG_ACCESS_TOKEN", defaultIgAccessToken);
    }

    public String getIgUserId() {
        return get("IG_USER_ID", defaultIgUserId);
    }
}
