package com.chehartemple.repository;

import com.chehartemple.model.SponsorMaster;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SponsorRepository extends JpaRepository<SponsorMaster, Long> {

    Page<SponsorMaster> findAllByOrderByPriorityOrderAscCreatedAtDesc(Pageable pageable);

    Page<SponsorMaster> findBySponsorStatusOrderByPriorityOrderAsc(SponsorStatus status, Pageable pageable);

    @Query("SELECT s FROM SponsorMaster s WHERE s.sponsorStatus = 'ACTIVE' ORDER BY s.priorityOrder ASC, s.displaySequence ASC")
    List<SponsorMaster> findActiveSponsors();

    @Query("SELECT s FROM SponsorMaster s WHERE s.sponsorStatus = 'UPCOMING' ORDER BY s.displayStartDateTime ASC")
    List<SponsorMaster> findUpcomingSponsors();

    @Query("SELECT s FROM SponsorMaster s WHERE s.sponsorStatus = 'EXPIRED' ORDER BY s.displayEndDateTime DESC")
    List<SponsorMaster> findExpiredSponsors();

    Page<SponsorMaster> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);

    @Query("SELECT s FROM SponsorMaster s WHERE s.displayStartDateTime >= :start AND s.displayEndDateTime <= :end ORDER BY s.displayStartDateTime ASC")
    Page<SponsorMaster> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // Scheduler: UPCOMING → ACTIVE
    @Query("SELECT s FROM SponsorMaster s WHERE s.sponsorStatus = 'UPCOMING' AND s.displayStartDateTime <= :now")
    List<SponsorMaster> findSponsorsDueToActivate(@Param("now") LocalDateTime now);

    // Scheduler: ACTIVE → EXPIRED
    @Query("SELECT s FROM SponsorMaster s WHERE s.sponsorStatus = 'ACTIVE' AND s.displayEndDateTime <= :now")
    List<SponsorMaster> findSponsorsDueToExpire(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE SponsorMaster s SET s.clickCount = s.clickCount + 1, s.updatedAt = :now WHERE s.id = :id")
    void incrementClickCount(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE SponsorMaster s SET s.deleted = true, s.updatedAt = :now WHERE s.id = :id")
    void softDelete(@Param("id") Long id, @Param("now") LocalDateTime now);
}
