package com.chehartemple.service;

import com.chehartemple.dto.InstagramDto.*;
import com.chehartemple.exception.ApiException;
import com.chehartemple.model.InstagramMedia;
import com.chehartemple.model.InstagramMedia.InstagramMediaType;
import com.chehartemple.repository.InstagramMediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramService {

    private final InstagramMediaRepository instagramMediaRepository;
    private final ConfigService configService;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GRAPH_API_BASE = "https://graph.facebook.com/v21.0";
    private static final String MEDIA_FIELDS = "id,caption,media_type,media_url,thumbnail_url,permalink,timestamp,username,like_count,comments_count,media_product_type,is_shared_to_feed";

    /**
     * Fetch media from Instagram Graph API and save/update in database.
     */
    public int syncMedia() {
        String accessToken = configService.getIgAccessToken();
        String instagramUserId = configService.getIgUserId();
        if (accessToken.isEmpty() || instagramUserId.isEmpty()) {
            log.warn("Instagram not configured: missing access-token or user-id");
            throw ApiException.badRequest("Instagram not configured. Set access-token and user-id.");
        }

        String url = String.format("%s/%s/media?fields=%s&limit=50&access_token=%s",
                GRAPH_API_BASE, instagramUserId, MEDIA_FIELDS, accessToken);

        int savedCount = 0;
        try {
            savedCount = fetchAndSave(url);
            log.info("Instagram sync completed. Saved/updated {} media items.", savedCount);
        } catch (Exception e) {
            log.error("Instagram sync failed: {}", e.getMessage(), e);
            throw ApiException.serverError("Instagram sync failed: " + e.getMessage());
        }
        return savedCount;
    }

    @SuppressWarnings("unchecked")
    private int fetchAndSave(String url) {
        int count = 0;
        while (url != null && !url.isEmpty()) {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("data")) break;

            List<Map<String, Object>> mediaList = (List<Map<String, Object>>) response.get("data");
            for (Map<String, Object> media : mediaList) {
                if (saveOrUpdateMedia(media)) count++;
            }

            // Pagination
            url = null;
            if (response.containsKey("paging")) {
                Map<String, Object> paging = (Map<String, Object>) response.get("paging");
                if (paging != null && paging.containsKey("next")) {
                    url = (String) paging.get("next");
                }
            }
        }
        return count;
    }

    private boolean saveOrUpdateMedia(Map<String, Object> media) {
        String mediaId = (String) media.get("id");
        String mediaTypeStr = (String) media.get("media_type");

        InstagramMediaType mediaType = mapMediaType(mediaTypeStr);
        if (mediaType == null) return false;

        String caption = (String) media.getOrDefault("caption", "");
        String mediaUrl = (String) media.getOrDefault("media_url", "");
        String thumbnailUrl = (String) media.getOrDefault("thumbnail_url", "");
        String permalink = (String) media.getOrDefault("permalink", "");
        String username = (String) media.getOrDefault("username", "");
        String timestampStr = (String) media.getOrDefault("timestamp", "");
        String mediaProductType = (String) media.getOrDefault("media_product_type", "");
        Long likeCount = toLong(media.get("like_count"));
        Long commentsCount = toLong(media.get("comments_count"));
        Boolean isSharedToFeed = media.get("is_shared_to_feed") instanceof Boolean b ? b : false;

        LocalDateTime timestamp = parseTimestamp(timestampStr);
        LocalDate postedDate = parsePostedDate(timestampStr);

        Optional<InstagramMedia> existing = instagramMediaRepository.findByInstagramMediaId(mediaId);
        if (existing.isPresent()) {
            InstagramMedia item = existing.get();
            item.setCaption(caption);
            item.setMediaUrl(mediaUrl);
            item.setThumbnailUrl(thumbnailUrl);
            item.setPermalink(permalink);
            item.setUsername(username);
            item.setTimestamp(timestamp);
            item.setPostedDate(postedDate);
            item.setLikeCount(likeCount);
            item.setCommentsCount(commentsCount);
            item.setMediaProductType(mediaProductType);
            item.setIsSharedToFeed(isSharedToFeed);
            instagramMediaRepository.save(item);
        } else {
            InstagramMedia item = InstagramMedia.builder()
                    .instagramMediaId(mediaId)
                    .caption(caption)
                    .mediaType(mediaType)
                    .mediaUrl(mediaUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .permalink(permalink)
                    .username(username)
                    .timestamp(timestamp)
                    .postedDate(postedDate)
                    .likeCount(likeCount)
                    .commentsCount(commentsCount)
                    .mediaProductType(mediaProductType)
                    .isSharedToFeed(isSharedToFeed)
                    .active(true)
                    .build();
            instagramMediaRepository.save(item);
        }
        return true;
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number n) return n.longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return 0L; }
    }

    private InstagramMediaType mapMediaType(String type) {
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "IMAGE" -> InstagramMediaType.IMAGE;
            case "VIDEO" -> InstagramMediaType.VIDEO;
            case "REEL" -> InstagramMediaType.REEL;
            case "CAROUSEL_ALBUM" -> InstagramMediaType.CAROUSEL_ALBUM;
            default -> null;
        };
    }

    private LocalDateTime parseTimestamp(String ts) {
        if (ts == null || ts.isEmpty()) return LocalDateTime.now();
        try {
            return java.time.OffsetDateTime.parse(ts, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")).toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ex) {
                return LocalDateTime.now();
            }
        }
    }

    private LocalDate parsePostedDate(String ts) {
        if (ts == null || ts.isEmpty()) return LocalDate.now();
        try {
            return LocalDate.parse(ts.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    // --- Public API methods ---

    public PagedResponse getAllMedia(int page, int size) {
        Page<InstagramMedia> result = instagramMediaRepository
                .findByActiveTrueOrderByTimestampDesc(PageRequest.of(page, size));
        return toPagedResponse(result);
    }

    public PagedResponse getReels(int page, int size) {
        Page<InstagramMedia> result = instagramMediaRepository
                .findByMediaTypeAndActiveTrueOrderByTimestampDesc(InstagramMediaType.REEL, PageRequest.of(page, size));

        // Also include VIDEO type
        Page<InstagramMedia> videos = instagramMediaRepository
                .findByMediaTypeAndActiveTrueOrderByTimestampDesc(InstagramMediaType.VIDEO, PageRequest.of(page, size));

        // Merge and sort
        List<MediaResponse> combined = new ArrayList<>();
        combined.addAll(result.getContent().stream().map(this::toMediaResponse).toList());
        combined.addAll(videos.getContent().stream().map(this::toMediaResponse).toList());
        combined.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return PagedResponse.builder()
                .items(combined.stream().limit(size).toList())
                .currentPage(page)
                .totalPages(Math.max(result.getTotalPages(), videos.getTotalPages()))
                .totalItems(result.getTotalElements() + videos.getTotalElements())
                .hasNext(result.hasNext() || videos.hasNext())
                .build();
    }

    public PagedResponse getImages(int page, int size) {
        Page<InstagramMedia> result = instagramMediaRepository
                .findByMediaTypeAndActiveTrueOrderByTimestampDesc(InstagramMediaType.IMAGE, PageRequest.of(page, size));
        return toPagedResponse(result);
    }

    public MediaResponse getMediaById(UUID id) {
        InstagramMedia media = instagramMediaRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Media not found."));
        return toMediaResponse(media);
    }

    public void markAsAddedToApp(String instagramMediaId) {
        instagramMediaRepository.findByInstagramMediaId(instagramMediaId).ifPresent(media -> {
            media.setAddedToApp(true);
            instagramMediaRepository.save(media);
        });
    }

    public void markAsRemovedFromApp(String instagramMediaId) {
        instagramMediaRepository.findByInstagramMediaId(instagramMediaId).ifPresent(media -> {
            media.setAddedToApp(false);
            instagramMediaRepository.save(media);
        });
    }

    public List<MediaResponse> getLatestMedia(int limit) {
        return instagramMediaRepository.findTop20ByActiveTrueOrderByTimestampDesc()
                .stream().limit(limit).map(this::toMediaResponse).toList();
    }

    /**
     * Get media grouped by posted date with date-based pagination.
     * @param offset number of days to skip (0 = start from latest)
     * @param days number of days to fetch (default 5)
     */
    public DateGroupedResponse getMediaByDateGroup(int offset, int days) {
        List<LocalDate> allDates = instagramMediaRepository.findDistinctPostedDates();
        if (allDates.isEmpty()) {
            return DateGroupedResponse.builder()
                    .groupedByDate(Collections.emptyMap())
                    .daysOffset(offset).daysCount(days).hasMore(false).build();
        }

        int fromIndex = offset;
        int toIndex = Math.min(offset + days, allDates.size());
        boolean hasMore = toIndex < allDates.size();

        if (fromIndex >= allDates.size()) {
            return DateGroupedResponse.builder()
                    .groupedByDate(Collections.emptyMap())
                    .daysOffset(offset).daysCount(days).hasMore(false).build();
        }

        List<LocalDate> targetDates = allDates.subList(fromIndex, toIndex);
        LocalDate startDate = targetDates.get(targetDates.size() - 1);
        LocalDate endDate = targetDates.get(0);

        List<InstagramMedia> media = instagramMediaRepository
                .findByActiveTrueAndPostedDateBetweenOrderByTimestampDesc(startDate, endDate);

        Map<String, List<MediaResponse>> grouped = new LinkedHashMap<>();
        for (LocalDate date : targetDates) {
            String dateKey = date.toString();
            List<MediaResponse> items = media.stream()
                    .filter(m -> date.equals(m.getPostedDate()))
                    .map(this::toMediaResponse)
                    .toList();
            if (!items.isEmpty()) grouped.put(dateKey, items);
        }

        return DateGroupedResponse.builder()
                .groupedByDate(grouped)
                .daysOffset(offset).daysCount(days).hasMore(hasMore).build();
    }

    // --- Helpers ---

    private PagedResponse toPagedResponse(Page<InstagramMedia> page) {
        return PagedResponse.builder()
                .items(page.getContent().stream().map(this::toMediaResponse).toList())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .hasNext(page.hasNext())
                .build();
    }

    private MediaResponse toMediaResponse(InstagramMedia media) {
        return MediaResponse.builder()
                .id(media.getId())
                .instagramMediaId(media.getInstagramMediaId())
                .caption(media.getCaption())
                .mediaType(media.getMediaType().name())
                .mediaUrl(media.getMediaUrl())
                .thumbnailUrl(media.getThumbnailUrl())
                .permalink(media.getPermalink())
                .username(media.getUsername())
                .timestamp(media.getTimestamp())
                .postedDate(media.getPostedDate())
                .likeCount(media.getLikeCount())
                .commentsCount(media.getCommentsCount())
                .mediaProductType(media.getMediaProductType())
                .addedToApp(media.isAddedToApp())
                .build();
    }
}
