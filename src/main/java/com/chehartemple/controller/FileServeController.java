package com.chehartemple.controller;

import com.chehartemple.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileServeController {

    private final FileStorageService fileStorageService;

    /**
     * GET /api/files/sponsors/uuid.jpg
     * Serves the file bytes with correct Content-Type.
     * Publicly accessible — no auth required.
     */
    @GetMapping("/{folder}/{filename}")
    public ResponseEntity<byte[]> serveFile(
            @PathVariable String folder,
            @PathVariable String filename) {

        String relativePath = folder + "/" + filename;
        byte[] data = fileStorageService.read(relativePath);
        String contentType = fileStorageService.getContentType(relativePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(data);
    }
}
