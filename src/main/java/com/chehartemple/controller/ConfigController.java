package com.chehartemple.controller;

import com.chehartemple.model.AppConfig;
import com.chehartemple.repository.AppConfigRepository;
import com.chehartemple.service.AuditService;
import com.chehartemple.service.FacebookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ConfigController {

    private final AppConfigRepository repository;
    private final FacebookService facebookService;
    private final AuditService auditService;

    // Public: get live stream URL (tries config first, then latest FB video)
    @GetMapping("/live-stream")
    public Map<String, String> getLiveStream() {
        String url = repository.findByConfigKey("LIVE_STREAM_URL")
                .map(AppConfig::getConfigValue).orElse("");
        // If no manual URL set, fetch latest video from Facebook page
        if (url == null || url.isEmpty()) {
            //url = facebookService.getLatestVideoSource();
        }
        return Map.of("url", url);
    }

    // Public: get all social media links
    @GetMapping("/config/social-media")
    public Map<String, String> getSocialMedia() {
        return Map.of(
            "facebook", getConfigValue("FACEBOOK_URL"),
            "instagram", getConfigValue("INSTAGRAM_URL"),
            "youtube", getConfigValue("YOUTUBE_URL")
        );
    }

    // Public: get contact information
    @GetMapping("/config/contact-info")
    public Map<String, String> getContactInfo() {
        return Map.of(
            "email", getConfigValue("CONTACT_EMAIL"),
            "phone", getConfigValue("CONTACT_PHONE"),
            "address", getConfigValue("TEMPLE_ADDRESS")
        );
    }

    // Admin: get all configs
    @GetMapping("/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppConfig> getAll() {
        return repository.findAll();
    }

    // Admin: update config
    @PostMapping("/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public AppConfig upsertConfig(@RequestBody AppConfig config, HttpServletRequest request) {
        repository.findByConfigKey(config.getConfigKey()).ifPresent(existing ->
            config.setId(existing.getId())
        );
        AppConfig saved = repository.save(config);
        auditService.log("UPDATE_CONFIG", "CONFIG", config.getConfigKey(), "Updated config: " + config.getConfigKey(), "ADMIN", request);
        return saved;
    }

    private String getConfigValue(String key) {
        return repository.findByConfigKey(key).map(AppConfig::getConfigValue).orElse("");
    }
}
