package com.chehartemple.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken() {
        return ResponseEntity.ok(Map.of("valid", "true"));
    }
}
