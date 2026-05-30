package com.chehartemple.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instagram_media", indexes = {
    @Index(name = "idx_ig_media_id", columnList = "instagramMediaId", unique = true),
    @Index(name = "idx_ig_media_type", columnList = "mediaType"),
    @Index(name = "idx_ig_timestamp", columnList = "timestamp"),
    @Index(name = "idx_ig_posted_date", columnList = "postedDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstagramMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String instagramMediaId;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstagramMediaType mediaType;

    @Column(columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String permalink;

    private String username;

    private LocalDateTime timestamp;

    @Column(columnDefinition = "DATE")
    private LocalDate postedDate;

    @Builder.Default
    private Long likeCount = 0L;

    @Builder.Default
    private Long commentsCount = 0L;

    private String mediaProductType;

    @Builder.Default
    private Boolean isSharedToFeed = false;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean addedToApp = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public enum InstagramMediaType { IMAGE, VIDEO, REEL, CAROUSEL_ALBUM }
}
