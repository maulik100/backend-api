package com.chehartemple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheharTempleApplication {
    public static void main(String[] args) {
        SpringApplication.run(CheharTempleApplication.class, args);
    }
}
