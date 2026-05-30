package com.chehartemple.controller;

import com.chehartemple.service.FacebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FacebookVideoController {

    private final FacebookService facebookService;

    /**
     * Page videos with direct MP4 source URLs.
     */
    @GetMapping("/facebook/videos")
    public Map<String, Object> getFacebookVideos(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        List<Map<String, Object>> raw = facebookService.getPageVideos(limit, offset);
        List<Map<String, Object>> items = raw.stream().map(v -> {
            Map<String, Object> item = new LinkedHashMap<>();
            String videoId = String.valueOf(v.get("id"));
            String permalinkUrl = v.containsKey("permalink_url") ? String.valueOf(v.get("permalink_url")) : "/videos/" + videoId;
            String fbVideoUrl = "https://www.facebook.com" + permalinkUrl;
            item.put("id", videoId);
            item.put("title", v.getOrDefault("title", v.getOrDefault("description", "Video")));
            item.put("videoUrl", v.getOrDefault("source", ""));
            item.put("embedUrl", "https://www.facebook.com/plugins/video.php?href=" + URLEncoder.encode(fbVideoUrl, StandardCharsets.UTF_8) + "&width=500&show_text=false");
            item.put("thumbnail", extractThumbnail(v));
            item.put("createdTime", v.getOrDefault("created_time", ""));
            item.put("type", "VIDEO");
            return item;
        }).collect(Collectors.toList());

        return Map.of("items", items, "hasMore", raw.size() == limit);
    }

    /**
     * Page posts with images and captions (from /feed).
     * Falls back to attachments if full_picture is null.
     */
    @GetMapping("/facebook/posts")
    public Map<String, Object> getFacebookPosts(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        List<Map<String, Object>> raw = facebookService.getPagePosts(limit, offset);
        List<Map<String, Object>> items = raw.stream()
            .map(p -> {
                String imageUrl = extractImageFromPost(p);
                if (imageUrl == null || imageUrl.isEmpty()) return null;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", p.get("id"));
                item.put("caption", p.getOrDefault("message", ""));
                item.put("imageUrl", imageUrl);
                item.put("createdTime", p.getOrDefault("created_time", ""));
                item.put("type", "IMAGE");
                return item;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return Map.of("items", items, "hasMore", raw.size() == limit);
    }

    /**
     * Page photos directly from /photos edge (most reliable for images).
     */
    @GetMapping("/facebook/photos")
    public Map<String, Object> getFacebookPhotos(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        List<Map<String, Object>> raw = facebookService.getPagePhotos(limit, offset);
        List<Map<String, Object>> items = raw.stream().map(photo -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", photo.get("id"));
            item.put("caption", photo.getOrDefault("name", ""));
            item.put("imageUrl", extractBestImage(photo));
            item.put("createdTime", photo.getOrDefault("created_time", ""));
            item.put("type", "IMAGE");
            return item;
        }).filter(i -> i.get("imageUrl") != null && !((String)i.get("imageUrl")).isEmpty())
          .collect(Collectors.toList());

        return Map.of("items", items, "hasMore", raw.size() == limit);
    }

    /**
     * Page reels with direct MP4 source URLs.
     */
    @GetMapping("/facebook/reels")
    public Map<String, Object> getFacebookReels(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        List<Map<String, Object>> raw = facebookService.getPageReels(limit, offset);
        List<Map<String, Object>> items = raw.stream().map(r -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.get("id"));
            item.put("title", r.getOrDefault("description", "Reel"));
            item.put("videoUrl", r.getOrDefault("source", ""));
            item.put("thumbnail", extractThumbnail(r));
            item.put("createdTime", r.getOrDefault("created_time", ""));
            item.put("type", "REEL");
            return item;
        }).collect(Collectors.toList());

        return Map.of("items", items, "hasMore", raw.size() == limit);
    }

    /**
     * Latest video source URL for live darshan.
     */
    @GetMapping("/facebook/latest")
    public Map<String, String> getLatestVideo() {
        return Map.of("url", facebookService.getLatestVideoSource());
    }

    /**
     * Admin: Exchange short-lived token for long-lived one.
     */
    @PostMapping("/admin/facebook/exchange-token")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> exchangeToken(@RequestBody Map<String, String> body) {
        String shortToken = body.get("token");
        if (shortToken == null || shortToken.isEmpty()) return Map.of("error", "Token is required");
        String longToken = facebookService.exchangeForLongLivedToken(shortToken);
        if (longToken.isEmpty()) return Map.of("error", "Token exchange failed");
        return Map.of("longLivedToken", longToken);
    }

    @SuppressWarnings("unchecked")
    private String extractThumbnail(Map<String, Object> video) {
        Object thumbnails = video.get("thumbnails");
        if (thumbnails instanceof Map) {
            Object data = ((Map<?, ?>) thumbnails).get("data");
            if (data instanceof List && !((List<?>) data).isEmpty()) {
                Object first = ((List<?>) data).get(0);
                if (first instanceof Map) {
                    Object uri = ((Map<?, ?>) first).get("uri");
                    if (uri != null) return String.valueOf(uri);
                }
            }
        }
        return "";
    }

    /**
     * Extract image URL from a feed post.
     * Tries: full_picture → attachments.media.image.src
     */
    @SuppressWarnings("unchecked")
    private String extractImageFromPost(Map<String, Object> post) {
        // Try full_picture first
        Object fullPic = post.get("full_picture");
        if (fullPic != null && !fullPic.toString().isEmpty()) return fullPic.toString();

        // Try attachments → media → image → src
        Object attachments = post.get("attachments");
        if (attachments instanceof Map) {
            Object data = ((Map<?, ?>) attachments).get("data");
            if (data instanceof List && !((List<?>) data).isEmpty()) {
                Object first = ((List<?>) data).get(0);
                if (first instanceof Map) {
                    Object media = ((Map<?, ?>) first).get("media");
                    if (media instanceof Map) {
                        Object image = ((Map<?, ?>) media).get("image");
                        if (image instanceof Map) {
                            Object src = ((Map<?, ?>) image).get("src");
                            if (src != null) return src.toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extract best quality image from /photos response.
     * The 'images' field contains array sorted by size (largest first).
     */
    @SuppressWarnings("unchecked")
    private String extractBestImage(Map<String, Object> photo) {
        Object images = photo.get("images");
        if (images instanceof List && !((List<?>) images).isEmpty()) {
            Object first = ((List<?>) images).get(0); // largest image
            if (first instanceof Map) {
                Object src = ((Map<?, ?>) first).get("source");
                if (src != null) return src.toString();
            }
        }
        return "";
    }
}
