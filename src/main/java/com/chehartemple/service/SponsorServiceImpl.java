package com.chehartemple.service;

import com.chehartemple.dto.*;
import com.chehartemple.exception.ApiException;
import com.chehartemple.model.SponsorMaster;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import com.chehartemple.repository.SponsorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SponsorServiceImpl implements SponsorService {

    private final SponsorRepository sponsorRepository;
    private final FileStorageService fileStorageService;

    private static final String UPLOAD_FOLDER = "sponsors";

    @Override
    @Transactional
    public SponsorResponseDTO createSponsor(SponsorCreateRequestDTO dto,
                                            MultipartFile mediaFile,
                                            MultipartFile thumbnailFile,
                                            String createdBy) {
        validateDates(dto.getDisplayStartDateTime(), dto.getDisplayEndDateTime());

        String mediaPath = fileStorageService.save(mediaFile, UPLOAD_FOLDER);
        String thumbPath = (thumbnailFile != null && !thumbnailFile.isEmpty())
                ? fileStorageService.save(thumbnailFile, UPLOAD_FOLDER) : null;

        SponsorMaster sponsor = SponsorMaster.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .mediaType(dto.getMediaType())
                .mediaLink(mediaPath)
                .thumbnailLink(thumbPath)
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
        return SponsorResponseDTO.from(saved);
    }

    @Override
    @Transactional
    public SponsorResponseDTO updateSponsor(Long id,
                                            SponsorUpdateRequestDTO dto,
                                            MultipartFile mediaFile,
                                            MultipartFile thumbnailFile,
                                            String updatedBy) {
        SponsorMaster sponsor = findById(id);

        if (dto.getTitle() != null)         sponsor.setTitle(dto.getTitle());
        if (dto.getDescription() != null)   sponsor.setDescription(dto.getDescription());
        if (dto.getMediaType() != null)     sponsor.setMediaType(dto.getMediaType());
        if (dto.getRedirectUrl() != null)   sponsor.setRedirectUrl(dto.getRedirectUrl());
        if (dto.getPriorityOrder() != null) sponsor.setPriorityOrder(dto.getPriorityOrder());
        if (dto.getDisplaySequence() != null) sponsor.setDisplaySequence(dto.getDisplaySequence());
        if (dto.getSponsorStatus() != null) sponsor.setSponsorStatus(dto.getSponsorStatus());
        if (dto.getRemarks() != null)       sponsor.setRemarks(dto.getRemarks());

        // Replace media file if a new one is provided
        if (mediaFile != null && !mediaFile.isEmpty()) {
            fileStorageService.delete(sponsor.getMediaLink());
            sponsor.setMediaLink(fileStorageService.save(mediaFile, UPLOAD_FOLDER));
        }

        // Replace thumbnail if a new one is provided
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            fileStorageService.delete(sponsor.getThumbnailLink());
            sponsor.setThumbnailLink(fileStorageService.save(thumbnailFile, UPLOAD_FOLDER));
        }

        LocalDateTime start = dto.getDisplayStartDateTime() != null
                ? dto.getDisplayStartDateTime() : sponsor.getDisplayStartDateTime();
        LocalDateTime end = dto.getDisplayEndDateTime() != null
                ? dto.getDisplayEndDateTime() : sponsor.getDisplayEndDateTime();
        validateDates(start, end);
        sponsor.setDisplayStartDateTime(start);
        sponsor.setDisplayEndDateTime(end);
        sponsor.setUpdatedBy(updatedBy);

        SponsorMaster saved = sponsorRepository.save(sponsor);
        log.info("Sponsor updated: id={}, by={}", id, updatedBy);
        return SponsorResponseDTO.from(saved);
    }

    @Override
    @Transactional
    public void deleteSponsor(Long id) {
        SponsorMaster sponsor = findById(id);
        fileStorageService.delete(sponsor.getMediaLink());
        fileStorageService.delete(sponsor.getThumbnailLink());
        sponsorRepository.softDelete(id, LocalDateTime.now());
        log.info("Sponsor soft-deleted: id={}", id);
    }

    @Override
    public SponsorResponseDTO getSponsorById(Long id) {
        return SponsorResponseDTO.from(findById(id));
    }

    @Override
    public SponsorListResponseDTO getAllSponsors(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return toListDto(sponsorRepository.findAll(PageRequest.of(page, size, sort)));
    }

    @Override
    public SponsorListResponseDTO getSponsorsByStatus(SponsorStatus status, int page, int size) {
        return toListDto(sponsorRepository.findBySponsorStatusOrderByPriorityOrderAsc(
                status, PageRequest.of(page, size, Sort.by("priorityOrder").ascending())));
    }

    @Override
    public List<SponsorResponseDTO> getActiveSponsors() {
        return sponsorRepository.findActiveSponsors().stream()
                .map(SponsorResponseDTO::from).collect(Collectors.toList());
    }

    @Override
    public List<SponsorResponseDTO> getUpcomingSponsors() {
        return sponsorRepository.findUpcomingSponsors().stream()
                .map(SponsorResponseDTO::from).collect(Collectors.toList());
    }

    @Override
    public List<SponsorResponseDTO> getExpiredSponsors() {
        return sponsorRepository.findExpiredSponsors().stream()
                .map(SponsorResponseDTO::from).collect(Collectors.toList());
    }

    @Override
    public SponsorPreviewDTO previewSponsor(Long id) {
        SponsorMaster s = findById(id);
        return SponsorPreviewDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .mediaType(s.getMediaType())
                .mediaUrl(s.getMediaLink() != null ? "/api/files/" + s.getMediaLink() : null)
                .thumbnailUrl(s.getThumbnailLink() != null ? "/api/files/" + s.getThumbnailLink() : null)
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
        log.info("Sponsor status updated: id={}, status={}, by={}", id, status, updatedBy);
        return SponsorResponseDTO.from(sponsorRepository.save(sponsor));
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

    private SponsorMaster findById(Long id) {
        return sponsorRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Sponsor not found with id: " + id));
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw ApiException.badRequest("End date/time must be after start date/time");
        }
    }

    private SponsorListResponseDTO toListDto(Page<SponsorMaster> page) {
        return SponsorListResponseDTO.builder()
                .sponsors(page.getContent().stream().map(SponsorResponseDTO::from).collect(Collectors.toList()))
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
