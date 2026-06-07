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
    private String mediaLink;
    private String mediaPreviewUrl;   // converted Google Drive preview URL
    private String thumbnailLink;
    private String thumbnailPreviewUrl;
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

    public static SponsorResponseDTO from(SponsorMaster s, String previewUrl, String thumbPreviewUrl) {
        return SponsorResponseDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .mediaType(s.getMediaType())
                .mediaLink(s.getMediaLink())
                .mediaPreviewUrl(previewUrl)
                .thumbnailLink(s.getThumbnailLink())
                .thumbnailPreviewUrl(thumbPreviewUrl)
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
}
