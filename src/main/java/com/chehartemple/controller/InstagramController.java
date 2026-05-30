package com.chehartemple.controller;

import com.chehartemple.dto.InstagramDto.*;
import com.chehartemple.service.InstagramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/instagram")
@RequiredArgsConstructor
public class InstagramController {

    private final InstagramService instagramService;

    /**
     * Admin: Sync media from Instagram Graph API.
     */
    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> syncMedia() {
        int count = instagramService.syncMedia();
        return ResponseEntity.ok(ApiResponse.ok("Instagram media synced successfully. " + count + " items processed.", count));
    }

    /**
     * Admin: Refresh (re-sync) media.
     */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Integer>> refreshMedia() {
        int count = instagramService.syncMedia();
        return ResponseEntity.ok(ApiResponse.ok("Instagram media refreshed. " + count + " items updated.", count));
    }

    /**
     * Public: Get all media (paginated).
     */
    @GetMapping("/media")
    public ResponseEntity<ApiResponse<PagedResponse>> getAllMedia(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse data = instagramService.getAllMedia(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Instagram media fetched successfully.", data));
    }

    /**
     * Public: Get reels/videos only (paginated).
     */
    @GetMapping("/reels")
    public ResponseEntity<ApiResponse<PagedResponse>> getReels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse data = instagramService.getReels(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Instagram reels fetched successfully.", data));
    }

    /**
     * Public: Get images only (paginated).
     */
    @GetMapping("/images")
    public ResponseEntity<ApiResponse<PagedResponse>> getImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse data = instagramService.getImages(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Instagram images fetched successfully.", data));
    }

    /**
     * Public: Get media by ID.
     */
    @GetMapping("/media/{id}")
    public ResponseEntity<ApiResponse<MediaResponse>> getMediaById(@PathVariable UUID id) {
        MediaResponse data = instagramService.getMediaById(id);
        return ResponseEntity.ok(ApiResponse.ok("Media fetched successfully.", data));
    }

    /**
     * Admin: Mark Instagram media as added to app.
     */
    @PostMapping("/mark-added/{instagramMediaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> markAdded(@PathVariable String instagramMediaId) {
        instagramService.markAsAddedToApp(instagramMediaId);
        return ResponseEntity.ok(ApiResponse.ok("Marked as added.", "success"));
    }

    /**
     * Admin: Mark Instagram media as removed from app.
     */
    @PostMapping("/mark-removed/{instagramMediaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> markRemoved(@PathVariable String instagramMediaId) {
        instagramService.markAsRemovedFromApp(instagramMediaId);
        return ResponseEntity.ok(ApiResponse.ok("Marked as removed.", "success"));
    }

    /**
     * Public: Get media grouped by posted date (date-based pagination).
     * offset = number of date-groups to skip, days = number of date-groups to return.
     */
    @GetMapping("/media/by-date")
    public ResponseEntity<ApiResponse<DateGroupedResponse>> getMediaByDate(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "5") int days) {
        DateGroupedResponse data = instagramService.getMediaByDateGroup(offset, days);
        return ResponseEntity.ok(ApiResponse.ok("Instagram media fetched by date.", data));
    }
}
