package com.chehartemple.repository;

import com.chehartemple.model.InstagramMedia;
import com.chehartemple.model.InstagramMedia.InstagramMediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstagramMediaRepository extends JpaRepository<InstagramMedia, UUID> {

    List<InstagramMedia> findByActiveTrueOrderByTimestampDesc();

    Page<InstagramMedia> findByActiveTrueOrderByTimestampDesc(Pageable pageable);

    Page<InstagramMedia> findByMediaTypeAndActiveTrueOrderByTimestampDesc(InstagramMediaType mediaType, Pageable pageable);

    List<InstagramMedia> findTop20ByActiveTrueOrderByTimestampDesc();

    Optional<InstagramMedia> findByInstagramMediaId(String instagramMediaId);

    boolean existsByInstagramMediaId(String instagramMediaId);

    List<InstagramMedia> findByMediaTypeAndActiveTrueOrderByTimestampDesc(InstagramMediaType mediaType);

    List<InstagramMedia> findByActiveTrueAndPostedDateBetweenOrderByTimestampDesc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT m.postedDate FROM InstagramMedia m WHERE m.active = true ORDER BY m.postedDate DESC")
    List<LocalDate> findDistinctPostedDates();

    List<InstagramMedia> findByActiveTrueAndAddedToAppFalseOrderByTimestampDesc();

    Optional<InstagramMedia> findByMediaUrl(String mediaUrl);

    Optional<InstagramMedia> findByPermalink(String permalink);
}
