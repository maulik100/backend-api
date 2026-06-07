package com.chehartemple.service;

import com.chehartemple.dto.*;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SponsorService {
    SponsorResponseDTO createSponsor(SponsorCreateRequestDTO dto, MultipartFile mediaFile, MultipartFile thumbnailFile, String createdBy);
    SponsorResponseDTO updateSponsor(Long id, SponsorUpdateRequestDTO dto, MultipartFile mediaFile, MultipartFile thumbnailFile, String updatedBy);
    void deleteSponsor(Long id);
    SponsorResponseDTO getSponsorById(Long id);
    SponsorListResponseDTO getAllSponsors(int page, int size, String sortBy, String sortDir);
    SponsorListResponseDTO getSponsorsByStatus(SponsorStatus status, int page, int size);
    List<SponsorResponseDTO> getActiveSponsors();
    List<SponsorResponseDTO> getUpcomingSponsors();
    List<SponsorResponseDTO> getExpiredSponsors();
    SponsorPreviewDTO previewSponsor(Long id);
    SponsorResponseDTO updateStatus(Long id, SponsorStatus status, String updatedBy);
    void incrementClickCount(Long id);
    SponsorListResponseDTO searchSponsors(SponsorFilterDTO filter);
}
