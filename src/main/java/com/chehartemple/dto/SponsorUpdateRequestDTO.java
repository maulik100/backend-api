package com.chehartemple.dto;

import com.chehartemple.model.SponsorMaster.MediaType;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SponsorUpdateRequestDTO {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private MediaType mediaType;

    private String mediaLink;

    private String thumbnailLink;

    private LocalDateTime displayStartDateTime;

    private LocalDateTime displayEndDateTime;

    private SponsorStatus sponsorStatus;

    private Integer priorityOrder;

    private String redirectUrl;

    private Integer displaySequence;

    private String remarks;
}
