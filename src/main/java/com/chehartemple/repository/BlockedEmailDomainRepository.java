package com.chehartemple.repository;

import com.chehartemple.model.BlockedEmailDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BlockedEmailDomainRepository extends JpaRepository<BlockedEmailDomain, Long> {
    boolean existsByDomain(String domain);

    @Query("SELECT b.domain FROM BlockedEmailDomain b")
    List<String> findAllDomains();
}
