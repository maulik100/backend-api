package com.chehartemple.controller;

import com.chehartemple.dto.*;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import com.chehartemple.model.User;
import com.chehartemple.service.SponsorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sponsors")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorService sponsorService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveSponsors() {
        return ok("Active sponsors fetched", sponsorService.getActiveSponsors());
    }

    @PostMapping("/{id}/click")
    public ResponseEntity<Map<String, Object>> recordClick(@PathVariable Long id) {
        sponsorService.incrementClickCount(id);
        return ok("Click recorded", null);
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/sponsors/admin/create
     * multipart/form-data:
     *   - data  (JSON string of SponsorCreateRequestDTO)
     *   - media (required image/video file)
     *   - thumbnail (optional thumbnail image)
     */
    @PostMapping(value = "/admin/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> create(
            @RequestPart("data") String dataJson,
            @RequestPart("media") MultipartFile mediaFile,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,
            Authentication auth) throws Exception {

        SponsorCreateRequestDTO dto = objectMapper.readValue(dataJson, SponsorCreateRequestDTO.class);
        return ok("Sponsor created successfully",
                sponsorService.createSponsor(dto, mediaFile, thumbnailFile, getEmail(auth)));
    }

    /**
     * PUT /api/sponsors/admin/update/{id}
     * multipart/form-data:
     *   - data  (JSON string of SponsorUpdateRequestDTO)
     *   - media (optional — only if replacing the media file)
     *   - thumbnail (optional — only if replacing the thumbnail)
     */
    @PutMapping(value = "/admin/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "media", required = false) MultipartFile mediaFile,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,
            Authentication auth) throws Exception {

        SponsorUpdateRequestDTO dto = objectMapper.readValue(dataJson, SponsorUpdateRequestDTO.class);
        return ok("Sponsor updated successfully",
                sponsorService.updateSponsor(id, dto, mediaFile, thumbnailFile, getEmail(auth)));
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        sponsorService.deleteSponsor(id);
        return ok("Sponsor deleted successfully", null);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return ok("Sponsor fetched", sponsorService.getSponsorById(id));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        return ok("Sponsors fetched", sponsorService.getAllSponsors(page, size, sortBy, sortDirection));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getByStatus(
            @PathVariable SponsorStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ok("Sponsors fetched", sponsorService.getSponsorsByStatus(status, page, size));
    }

    @PatchMapping("/admin/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @RequestParam SponsorStatus status,
            Authentication auth) {
        return ok("Status updated", sponsorService.updateStatus(id, status, getEmail(auth)));
    }

    @GetMapping("/admin/preview/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> preview(@PathVariable Long id) {
        return ok("Preview fetched", sponsorService.previewSponsor(id));
    }

    @PostMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> search(@RequestBody SponsorFilterDTO filter) {
        return ok("Search results", sponsorService.searchSponsors(filter));
    }

    @GetMapping("/admin/upcoming")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> upcoming() {
        return ok("Upcoming sponsors", sponsorService.getUpcomingSponsors());
    }

    @GetMapping("/admin/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> expired() {
        return ok("Expired sponsors", sponsorService.getExpiredSponsors());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> ok(String message, Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("data", data);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(body);
    }

    private String getEmail(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof User u) return u.getEmail();
        return "ADMIN";
    }
}
