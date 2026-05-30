package com.chehartemple.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FacebookService {

    private final ConfigService configService;
    private final RestTemplate restTemplate = new RestTemplate();

    private String getAccessToken() {
        String token = configService.getFbPageAccessToken();
        if (token != null && !token.isEmpty()) return token;
        String appId = configService.getFbAppId();
        String appSecret = configService.getFbAppSecret();
        if (appId != null && !appId.isEmpty() && appSecret != null && !appSecret.isEmpty()) return appId + "|" + appSecret;
        return "";
    }

    private String getPageId() {
        return configService.getFbPageId();
    }

    private boolean isConfigured() {
        return !getAccessToken().isEmpty() && getPageId() != null && !getPageId().isEmpty();
    }

    public List<Map<String, Object>> getPageVideos(int limit, int offset) {
        if (!isConfigured()) return Collections.emptyList();
        String url = String.format(
            "https://graph.facebook.com/v21.0/%s/videos?fields=id,title,description,source,picture,thumbnails,created_time,length,permalink_url&limit=%d&offset=%d&access_token=%s",
            getPageId(), limit, offset, getAccessToken());
        return fetchData(url);
    }

    public List<Map<String, Object>> getPagePosts(int limit, int offset) {
        if (!isConfigured()) return Collections.emptyList();
        String url = String.format(
            "https://graph.facebook.com/v21.0/%s/feed?fields=id,message,full_picture,attachments{media{image{src}},description,title,type},created_time,type&limit=%d&offset=%d&access_token=%s",
            getPageId(), limit, offset, getAccessToken());
        return fetchData(url);
    }

    public List<Map<String, Object>> getPagePhotos(int limit, int offset) {
        if (!isConfigured()) return Collections.emptyList();
        String url = String.format(
            "https://graph.facebook.com/v21.0/%s/photos?type=uploaded&fields=id,name,images,created_time,link&limit=%d&offset=%d&access_token=%s",
            getPageId(), limit, offset, getAccessToken());
        return fetchData(url);
    }

    public List<Map<String, Object>> getPageReels(int limit, int offset) {
        if (!isConfigured()) return Collections.emptyList();
        String url = String.format(
            "https://graph.facebook.com/v21.0/%s/video_reels?fields=id,description,source,thumbnails,created_time&limit=%d&offset=%d&access_token=%s",
            getPageId(), limit, offset, getAccessToken());
        return fetchData(url);
    }

    public String getLatestVideoSource() {
        if (!isConfigured()) return "";
        String url = String.format(
            "https://graph.facebook.com/v21.0/%s/videos?fields=source,live_status&limit=1&access_token=%s",
            getPageId(), getAccessToken());
        List<Map<String, Object>> data = fetchData(url);
        if (!data.isEmpty()) return (String) data.get(0).getOrDefault("source", "");
        return "";
    }

    public String exchangeForLongLivedToken(String shortLivedToken) {
        String appId = configService.getFbAppId();
        String appSecret = configService.getFbAppSecret();
        String url = String.format(
            "https://graph.facebook.com/v21.0/oauth/access_token?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s",
            appId, appSecret, shortLivedToken);
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("access_token")) return (String) response.get("access_token");
        } catch (Exception e) { log.error("Token exchange failed: {}", e.getMessage()); }
        return "";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchData(String url) {
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("data")) return (List<Map<String, Object>>) response.get("data");
        } catch (Exception e) { log.error("Facebook API error: {}", e.getMessage()); }
        return Collections.emptyList();
    }
}
