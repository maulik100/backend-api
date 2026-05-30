package com.chehartemple.controller;

import com.chehartemple.model.GalleryItem;
import com.chehartemple.model.InstagramMedia;
import com.chehartemple.repository.GalleryRepository;
import com.chehartemple.repository.InstagramMediaRepository;
import com.chehartemple.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryRepository galleryRepository;
    private final InstagramMediaRepository instagramMediaRepository;
    private final AuditService auditService;

    // Full gallery (for admin) - paginated
    @GetMapping("/gallery")
    public Map<String, Object> getGallery(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GalleryItem> result = galleryRepository.findByActiveTrue(pageRequest);
        return Map.of(
            "content", result.getContent(),
            "currentPage", result.getNumber(),
            "totalPages", result.getTotalPages(),
            "totalElements", result.getTotalElements(),
            "hasNext", result.hasNext()
        );
    }

    // Paginated gallery for mobile (reels-style: 3 items per page)
    @GetMapping("/gallery/paged")
    public Map<String, Object> getPagedGallery(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "ALL") String type) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GalleryItem> result;

        if ("VIDEO".equals(type)) {
            result = galleryRepository.findByActiveTrueAndMediaType(GalleryItem.MediaType.VIDEO, pageRequest);
        } else if ("IMAGE".equals(type)) {
            result = galleryRepository.findByActiveTrueAndMediaType(GalleryItem.MediaType.IMAGE, pageRequest);
        } else {
            result = galleryRepository.findByActiveTrue(pageRequest);
        }

        return Map.of(
            "items", result.getContent(),
            "currentPage", result.getNumber(),
            "totalPages", result.getTotalPages(),
            "totalItems", result.getTotalElements(),
            "hasNext", result.hasNext()
        );
    }

    @PostMapping("/admin/gallery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody GalleryItem item, HttpServletRequest request) {
        if (galleryRepository.existsByUrl(item.getUrl())) {
            return ResponseEntity.badRequest().body(Map.of("error", "This item already exists in gallery"));
        }
        GalleryItem saved = galleryRepository.save(item);
        auditService.log("CREATE_GALLERY", "GALLERY", String.valueOf(saved.getId()), "Added gallery item: " + saved.getTitle(), "ADMIN", request);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/admin/gallery/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody GalleryItem item) {
        return galleryRepository.findById(id).map(existing -> {
            if (item.getEmbedUrl() != null) existing.setEmbedUrl(item.getEmbedUrl());
            if (item.getTitle() != null) existing.setTitle(item.getTitle());
            return ResponseEntity.ok(galleryRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/gallery/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        galleryRepository.findById(id).ifPresent(item -> {
            if (item.getSource() == GalleryItem.MediaSource.INSTAGRAM) {
                // Try matching by mediaUrl first, then by permalink (embedUrl)
                instagramMediaRepository.findByMediaUrl(item.getUrl()).ifPresentOrElse(
                    ig -> { ig.setAddedToApp(false); instagramMediaRepository.save(ig); },
                    () -> {
                        if (item.getEmbedUrl() != null && !item.getEmbedUrl().isEmpty()) {
                            instagramMediaRepository.findByPermalink(item.getEmbedUrl()).ifPresent(ig -> {
                                ig.setAddedToApp(false);
                                instagramMediaRepository.save(ig);
                            });
                        }
                    }
                );
            }
        });
        auditService.log("DELETE_GALLERY", "GALLERY", String.valueOf(id), "Deleted gallery item ID: " + id, "ADMIN", request);
        galleryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
