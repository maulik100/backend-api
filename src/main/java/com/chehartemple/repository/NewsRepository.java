package com.chehartemple.repository;

import com.chehartemple.model.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    List<News> findByActiveTrueOrderByCreatedAtDesc();
}
