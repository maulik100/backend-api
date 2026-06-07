package com.chehartemple.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "sponsor_master",
    indexes = {
        @Index(name = "idx_sponsor_status", columnList = "sponsor_status"),
        @Index(name = "idx_sponsor_start_dt", columnList = "display_start_date_time"),
        @Index(name = "idx_sponsor_end_dt", columnList = "display_end_date_time"),
        @Index(name = "idx_sponsor_deleted", columnList = "deleted")
    }
)
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @NotBlank
    @Column(name = "media_link", nullable = false)
    private String mediaLink;

    @Column(name = "thumbnail_link")
    private String thumbnailLink;

    @NotNull
    @Column(name = "display_start_date_time", nullable = false)
    private LocalDateTime displayStartDateTime;

    @NotNull
    @Column(name = "display_end_date_time", nullable = false)
    private LocalDateTime displayEndDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sponsor_status", nullable = false, length = 20)
    @Builder.Default
    private SponsorStatus sponsorStatus = SponsorStatus.UPCOMING;

    @Column(name = "priority_order")
    @Builder.Default
    private Integer priorityOrder = 0;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Column(name = "click_count")
    @Builder.Default
    private Long clickCount = 0L;

    @Column(name = "display_sequence")
    @Builder.Default
    private Integer displaySequence = 0;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum MediaType {
        IMAGE, VIDEO
    }

    public enum SponsorStatus {
        UPCOMING, ACTIVE, INACTIVE, EXPIRED, CANCELLED
    }
}
