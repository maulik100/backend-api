package com.chehartemple.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gallery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String url;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType = MediaType.IMAGE;

    @Enumerated(EnumType.STRING)
    private MediaSource source = MediaSource.OTHER;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String embedUrl;

    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MediaType { IMAGE, VIDEO }
    public enum MediaSource { FACEBOOK, INSTAGRAM, YOUTUBE, OTHER }
}
