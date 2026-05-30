package com.chehartemple.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate eventDate;
    private String startTime;   // e.g. "09:00 AM"
    private String endTime;     // e.g. "06:00 PM"
    private boolean allDayEvent = false;
    private String imageUrl;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
