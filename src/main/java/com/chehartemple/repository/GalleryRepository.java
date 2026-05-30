package com.chehartemple.repository;

import com.chehartemple.model.GalleryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GalleryRepository extends JpaRepository<GalleryItem, Long> {
    List<GalleryItem> findByActiveTrueOrderByCreatedAtDesc();
    Page<GalleryItem> findByActiveTrueAndMediaType(GalleryItem.MediaType mediaType, Pageable pageable);
    Page<GalleryItem> findByActiveTrue(Pageable pageable);
    boolean existsByUrl(String url);
    Optional<GalleryItem> findByUrl(String url);
}
