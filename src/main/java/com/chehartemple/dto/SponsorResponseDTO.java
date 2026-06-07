package com.chehartemple.dto;

import com.chehartemple.model.SponsorMaster;
import com.chehartemple.model.SponsorMaster.MediaType;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SponsorResponseDTO {

    private Long id;
    private String title;
    private String description;
    private MediaType mediaType;
    private String mediaPath;       // relative path stored in DB  e.g. sponsors/uuid.jpg
    private String mediaUrl;        // full serve URL              e.g. /api/files/sponsors/uuid.jpg
    private String thumbnailPath;
    private String thumbnailUrl;
    private LocalDateTime displayStartDateTime;
    private LocalDateTime displayEndDateTime;
    private SponsorStatus sponsorStatus;
    private Integer priorityOrder;
    private String redirectUrl;
    private Long clickCount;
    private Integer displaySequence;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String remarks;

    public static SponsorResponseDTO from(SponsorMaster s) {
        return SponsorResponseDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .mediaType(s.getMediaType())
                .mediaPath(s.getMediaLink())
                .mediaUrl(toFileUrl(s.getMediaLink()))
                .thumbnailPath(s.getThumbnailLink())
                .thumbnailUrl(toFileUrl(s.getThumbnailLink()))
                .displayStartDateTime(s.getDisplayStartDateTime())
                .displayEndDateTime(s.getDisplayEndDateTime())
                .sponsorStatus(s.getSponsorStatus())
                .priorityOrder(s.getPriorityOrder())
                .redirectUrl(s.getRedirectUrl())
                .clickCount(s.getClickCount())
                .displaySequence(s.getDisplaySequence())
                .createdBy(s.getCreatedBy())
                .updatedBy(s.getUpdatedBy())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .remarks(s.getRemarks())
                .build();
    }

    private static String toFileUrl(String path) {
        if (path == null || path.isBlank()) return null;
        return "/api/files/" + path;
    }
}
