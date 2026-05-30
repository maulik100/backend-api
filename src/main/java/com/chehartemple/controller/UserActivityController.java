package com.chehartemple.controller;

import com.chehartemple.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class UserActivityController {

    private final AuditService auditService;

    @PostMapping("/track")
    public ResponseEntity<Void> trackActivity(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String screen = body.getOrDefault("screen", "Unknown");
        String action = body.getOrDefault("action", "SCREEN_VIEW");
        String description = body.getOrDefault("description", screen);
        String sessionToken = body.get("sessionToken");
        String location = body.get("location");
        String source = request.getHeader("X-Source");
        if (source == null) source = "MOBILE";

        auditService.logWithLocation(action, "ACTIVITY", null, description, source, sessionToken, location, request);
        return ResponseEntity.ok().build();
    }
}
