package com.chehartemple.dto;

import com.chehartemple.model.SponsorMaster.MediaType;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SponsorCreateRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Media type is required")
    private MediaType mediaType;

    @NotNull(message = "Start date/time is required")
    private LocalDateTime displayStartDateTime;

    @NotNull(message = "End date/time is required")
    private LocalDateTime displayEndDateTime;

    private SponsorStatus sponsorStatus = SponsorStatus.UPCOMING;
    private Integer priorityOrder = 0;
    private String redirectUrl;
    private Integer displaySequence = 0;
    private String remarks;
}
