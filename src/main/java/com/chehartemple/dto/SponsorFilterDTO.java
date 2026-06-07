package com.chehartemple.dto;

import com.chehartemple.model.SponsorMaster.MediaType;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SponsorFilterDTO {
    private String title;
    private SponsorStatus status;
    private MediaType mediaType;
    private LocalDateTime startFrom;
    private LocalDateTime startTo;
    private LocalDateTime endFrom;
    private LocalDateTime endTo;
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
