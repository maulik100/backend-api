package com.chehartemple.dto;

import com.chehartemple.model.SponsorMaster.MediaType;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SponsorPreviewDTO {
    private Long id;
    private String title;
    private String description;
    private MediaType mediaType;
    private String mediaPreviewUrl;
    private String thumbnailPreviewUrl;
    private String redirectUrl;
    private SponsorStatus sponsorStatus;
}
