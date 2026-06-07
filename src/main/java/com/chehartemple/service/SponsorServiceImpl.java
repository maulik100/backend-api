package com.chehartemple.service;

import com.chehartemple.dto.*;
import com.chehartemple.exception.ApiException;
import com.chehartemple.model.SponsorMaster;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import com.chehartemple.repository.SponsorRepository;
import com.chehartemple.util.GoogleDriveMediaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SponsorServiceImpl implements SponsorService {

    private final SponsorRepository sponsorRepository;

    @Override
    @Transactional
    public SponsorResponseDTO createSponsor(SponsorCreateRequestDTO dto, String createdBy) {
        validateDates(dto.getDisplayStartDateTime(), dto.getDisplayEndDateTime());
        GoogleDriveMediaUtil.validateGoogleDriveUrl(dto.getMediaLink());
        GoogleDriveMediaUtil.validateGoogleDriveUrl(dto.getThumbnailLink());

        SponsorMaster sponsor = SponsorMaster.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .mediaType(dto.getMediaType())
                .mediaLink(dto.getMediaLink())
                .thumbnailLink(dto.getThumbnailLink())
                .displayStartDateTime(dto.getDisplayStartDateTime())
                .displayEndDateTime(dto.getDisplayEndDateTime())
                .sponsorStatus(dto.getSponsorStatus() != null ? dto.getSponsorStatus() : SponsorStatus.UPCOMING)
                .priorityOrder(dto.getPriorityOrder() != null ? dto.getPriorityOrder() : 0)
                .redirectUrl(dto.getRedirectUrl())
                .displaySequence(dto.getDisplaySequence() != null ? dto.getDisplaySequence() : 0)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .remarks(dto.getRemarks())
                .build();

        SponsorMaster saved = sponsorRepository.save(sponsor);
        log.info("Sponsor created: id={}, title={}, by={}", saved.getId(), saved.getTitle(), createdBy);
        return toDto(saved);
    }

    @Override
    @Transactional
    public SponsorResponseDTO updateSponsor(Long id, SponsorUpdateRequestDTO dto, String updatedBy) {
        SponsorMaster sponsor = findById(id);

        if (dto.getTitle() != null) sponsor.setTitle(dto.getTitle());
        if (dto.getDescription() != null) sponsor.setDescription(dto.getDescription());
        if (dto.getMediaType() != null) sponsor.setMediaType(dto.getMediaType());
        if (dto.getMediaLink() != null) {
            GoogleDriveMediaUtil.validateGoogleDriveUrl(dto.getMediaLink());
            sponsor.setMediaLink(dto.getMediaLink());
        }
        if (dto.getThumbnailLink() != null) {
            GoogleDriveMediaUtil.validateGoogleDriveUrl(dto.getThumbnailLink());
            sponsor.setThumbnailLink(dto.getThumbnailLink());
        }
        if (dto.getRedirectUrl() != null) sponsor.setRedirectUrl(dto.getRedirectUrl());
        if (dto.getPriorityOrder() != null) sponsor.setPriorityOrder(dto.getPriorityOrder());
        if (dto.getDisplaySequence() != null) sponsor.setDisplaySequence(dto.getDisplaySequence());
        if (dto.getSponsorStatus() != null) sponsor.setSponsorStatus(dto.getSponsorStatus());
        if (dto.getRemarks() != null) sponsor.setRemarks(dto.getRemarks());

        LocalDateTime start = dto.getDisplayStartDateTime() != null ? dto.getDisplayStartDateTime() : sponsor.getDisplayStartDateTime();
        LocalDateTime end = dto.getDisplayEndDateTime() != null ? dto.getDisplayEndDateTime() : sponsor.getDisplayEndDateTime();
        validateDates(start, end);
        sponsor.setDisplayStartDateTime(start);
        sponsor.setDisplayEndDateTime(end);
        sponsor.setUpdatedBy(updatedBy);

        SponsorMaster saved = sponsorRepository.save(sponsor);
        log.info("Sponsor updated: id={}, by={}", id, updatedBy);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteSponsor(Long id) {
        findById(id); // ensure exists
        sponsorRepository.softDelete(id, LocalDateTime.now());
        log.info("Sponsor soft-deleted: id={}", id);
    }

    @Override
    public SponsorResponseDTO getSponsorById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public SponsorListResponseDTO getAllSponsors(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SponsorMaster> result = sponsorRepository.findAll(pageable);
        return toListDto(result);
    }

    @Override
    public SponsorListResponseDTO getSponsorsByStatus(SponsorStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("priorityOrder").ascending());
        Page<SponsorMaster> result = sponsorRepository.findBySponsorStatusOrderByPriorityOrderAsc(status, pageable);
        return toListDto(result);
    }

    @Override
    public List<SponsorResponseDTO> getActiveSponsors() {
        return sponsorRepository.findActiveSponsors().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SponsorResponseDTO> getUpcomingSponsors() {
        return sponsorRepository.findUpcomingSponsors().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<SponsorResponseDTO> getExpiredSponsors() {
        return sponsorRepository.findExpiredSponsors().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public SponsorPreviewDTO previewSponsor(Long id) {
        SponsorMaster s = findById(id);
        boolean isVideo = s.getMediaType() == SponsorMaster.MediaType.VIDEO;
        return SponsorPreviewDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .mediaType(s.getMediaType())
                .mediaPreviewUrl(GoogleDriveMediaUtil.toPreviewUrl(s.getMediaLink(), isVideo))
                .thumbnailPreviewUrl(s.getThumbnailLink() != null
                        ? GoogleDriveMediaUtil.toPreviewUrl(s.getThumbnailLink(), false) : null)
                .redirectUrl(s.getRedirectUrl())
                .sponsorStatus(s.getSponsorStatus())
                .build();
    }

    @Override
    @Transactional
    public SponsorResponseDTO updateStatus(Long id, SponsorStatus status, String updatedBy) {
        SponsorMaster sponsor = findById(id);
        sponsor.setSponsorStatus(status);
        sponsor.setUpdatedBy(updatedBy);
        SponsorMaster saved = sponsorRepository.save(sponsor);
        log.info("Sponsor status updated: id={}, status={}, by={}", id, status, updatedBy);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void incrementClickCount(Long id) {
        sponsorRepository.incrementClickCount(id, LocalDateTime.now());
    }

    @Override
    public SponsorListResponseDTO searchSponsors(SponsorFilterDTO filter) {
        Sort sort = filter.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filter.getSortBy()).ascending()
                : Sort.by(filter.getSortBy()).descending();
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<SponsorMaster> result;
        if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
            result = sponsorRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(filter.getTitle(), pageable);
        } else if (filter.getStatus() != null) {
            result = sponsorRepository.findBySponsorStatusOrderByPriorityOrderAsc(filter.getStatus(), pageable);
        } else if (filter.getStartFrom() != null && filter.getEndTo() != null) {
            result = sponsorRepository.findByDateRange(filter.getStartFrom(), filter.getEndTo(), pageable);
        } else {
            result = sponsorRepository.findAll(pageable);
        }
        return toListDto(result);
    }

    // ---------- helpers ----------

    private SponsorMaster findById(Long id) {
        return sponsorRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Sponsor not found with id: " + id));
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw ApiException.badRequest("End date/time must be after start date/time");
        }
    }

    private SponsorResponseDTO toDto(SponsorMaster s) {
        boolean isVideo = s.getMediaType() == SponsorMaster.MediaType.VIDEO;
        String previewUrl = GoogleDriveMediaUtil.toPreviewUrl(s.getMediaLink(), isVideo);
        String thumbUrl = s.getThumbnailLink() != null
                ? GoogleDriveMediaUtil.toPreviewUrl(s.getThumbnailLink(), false) : null;
        return SponsorResponseDTO.from(s, previewUrl, thumbUrl);
    }

    private SponsorListResponseDTO toListDto(Page<SponsorMaster> page) {
        return SponsorListResponseDTO.builder()
                .sponsors(page.getContent().stream().map(this::toDto).collect(Collectors.toList()))
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
