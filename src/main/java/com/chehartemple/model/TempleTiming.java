package com.chehartemple.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "temple_timings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempleTiming {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String day;

    @Column(nullable = false)
    private String openTime;

    @Column(nullable = false)
    private String closeTime;

    private String morningAartiTime;
    private String eveningAartiTime;
    private String specialNote;
}
