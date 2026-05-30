package com.chehartemple.repository;

import com.chehartemple.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    Optional<AppConfig> findByConfigKey(String key);
}
