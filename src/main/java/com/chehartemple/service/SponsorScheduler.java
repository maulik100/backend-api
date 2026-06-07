package com.chehartemple.service;

import com.chehartemple.model.SponsorMaster;
import com.chehartemple.model.SponsorMaster.SponsorStatus;
import com.chehartemple.repository.SponsorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SponsorScheduler {

    private final SponsorRepository sponsorRepository;

    // Runs every 5 minutes
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void syncSponsorStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // UPCOMING → ACTIVE
        List<SponsorMaster> toActivate = sponsorRepository.findSponsorsDueToActivate(now);
        if (!toActivate.isEmpty()) {
            toActivate.forEach(s -> {
                s.setSponsorStatus(SponsorStatus.ACTIVE);
                s.setUpdatedAt(now);
                s.setUpdatedBy("SCHEDULER");
            });
            sponsorRepository.saveAll(toActivate);
            log.info("SponsorScheduler: activated {} sponsors", toActivate.size());
        }

        // ACTIVE → EXPIRED
        List<SponsorMaster> toExpire = sponsorRepository.findSponsorsDueToExpire(now);
        if (!toExpire.isEmpty()) {
            toExpire.forEach(s -> {
                s.setSponsorStatus(SponsorStatus.EXPIRED);
                s.setUpdatedAt(now);
                s.setUpdatedBy("SCHEDULER");
            });
            sponsorRepository.saveAll(toExpire);
            log.info("SponsorScheduler: expired {} sponsors", toExpire.size());
        }
    }
}
