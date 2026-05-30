package com.chehartemple.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String configKey;

    @Column(columnDefinition = "TEXT")
    private String configValue;
}
