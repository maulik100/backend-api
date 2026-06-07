package com.chehartemple.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SponsorListResponseDTO {
    private List<SponsorResponseDTO> sponsors;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}
