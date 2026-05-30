package com.chehartemple.service;

import com.chehartemple.model.GalleryItem;
import com.chehartemple.model.InstagramMedia;
import com.chehartemple.repository.GalleryRepository;
import com.chehartemple.repository.InstagramMediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstagramScheduler {

    private final InstagramService instagramService;
    private final InstagramMediaRepository instagramMediaRepository;
    private final GalleryRepository galleryRepository;
    private final ConfigService configService;

    /**
     * Auto-sync Instagram media every 30 minutes.
     * After sync, auto-add any new media to the gallery (mobile app).
     */
    @Scheduled(fixedRate = 1800000, initialDelay = 60000)
    public void autoSyncInstagramMedia() {
        String accessToken = configService.getIgAccessToken();
        if (accessToken == null || accessToken.isEmpty()) return;
        try {
            int count = instagramService.syncMedia();
            log.info("[Scheduler] Instagram auto-sync completed. {} items processed.", count);
            int added = autoAddNewMediaToGallery();
            if (added > 0) {
                log.info("[Scheduler] Auto-added {} new Instagram items to gallery.", added);
            }
        } catch (Exception e) {
            log.error("[Scheduler] Instagram auto-sync failed: {}", e.getMessage());
        }
    }

    /**
     * Find all Instagram media not yet added to app and auto-insert into gallery.
     */
        private int autoAddNewMediaToGallery() {
        List<InstagramMedia> newMedia = instagramMediaRepository.findByActiveTrueAndAddedToAppFalseOrderByTimestampDesc();
        int added = 0;
        for (InstagramMedia media : newMedia) {
            if (media.getMediaUrl() == null || media.getMediaUrl().isEmpty()) continue;
            if (galleryRepository.existsByUrl(media.getMediaUrl())) {
                // Already in gallery, just mark as added
                media.setAddedToApp(true);
                instagramMediaRepository.save(media);
                continue;
            }
            try {
                String mediaType = (media.getMediaType() == InstagramMedia.InstagramMediaType.VIDEO
                        || media.getMediaType() == InstagramMedia.InstagramMediaType.REEL) ? "VIDEO" : "IMAGE";
                String title = (media.getCaption() != null && !media.getCaption().isEmpty())
                        ? media.getCaption().substring(0, Math.min(media.getCaption().length(), 100))
                        : media.getMediaType().name();

                GalleryItem item = GalleryItem.builder()
                        .title(title)
                        .url(media.getMediaUrl())
                        .mediaType(GalleryItem.MediaType.valueOf(mediaType))
                        .source(GalleryItem.MediaSource.INSTAGRAM)
                        .thumbnailUrl(media.getThumbnailUrl() != null ? media.getThumbnailUrl() : "")
                        .embedUrl(media.getPermalink() != null ? media.getPermalink() : "")
                        .active(true)
                        .build();
                galleryRepository.save(item);

                media.setAddedToApp(true);
                instagramMediaRepository.save(media);
                added++;
            } catch (Exception e) {
                log.warn("[Scheduler] Failed to auto-add media {}: {}", media.getInstagramMediaId(), e.getMessage());
            }
        }
        return added;
    }
}
