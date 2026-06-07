package com.chehartemple.service;

import com.chehartemple.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of("video/mp4", "video/webm", "video/ogg");
    private static final long MAX_IMAGE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_VIDEO_BYTES = 200 * 1024 * 1024; // 200 MB

    @Value("${app.upload.base-path:src/main/resources/uploads}")
    private String basePath;

    /**
     * Saves a file under basePath/folder/ with a UUID filename.
     * Returns the relative path stored in DB: folder/uuid.ext
     */
    public String save(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("File must not be empty");
        }

        String contentType = file.getContentType();
        boolean isVideo = contentType != null && contentType.startsWith("video/");

        if (isVideo) {
            if (!ALLOWED_VIDEO_TYPES.contains(contentType)) {
                throw ApiException.badRequest("Unsupported video type: " + contentType + ". Allowed: mp4, webm, ogg");
            }
            if (file.getSize() > MAX_VIDEO_BYTES) {
                throw ApiException.badRequest("Video file too large. Maximum allowed size is 200 MB");
            }
        } else {
            if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw ApiException.badRequest("Unsupported image type: " + contentType + ". Allowed: jpeg, png, webp, gif");
            }
            if (file.getSize() > MAX_IMAGE_BYTES) {
                throw ApiException.badRequest("Image file too large. Maximum allowed size is 10 MB");
            }
        }

        String ext = getExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID() + "." + ext;

        try {
            Path dir = Paths.get(basePath, folder);
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved: {}/{}", folder, filename);
            return folder + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw ApiException.serverError("Failed to save file. Please try again.");
        }
    }

    /**
     * Deletes a file by its stored relative path (e.g. sponsors/uuid.jpg).
     * Silently ignores if file does not exist.
     */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Path path = Paths.get(basePath, relativePath);
            Files.deleteIfExists(path);
            log.info("File deleted: {}", relativePath);
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", relativePath, e.getMessage());
        }
    }

    /**
     * Reads and returns the bytes of a stored file.
     * Throws 404 if not found.
     */
    public byte[] read(String relativePath) {
        try {
            Path path = Paths.get(basePath, relativePath);
            if (!Files.exists(path)) {
                throw ApiException.notFound("File not found: " + relativePath);
            }
            return Files.readAllBytes(path);
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to read file {}: {}", relativePath, e.getMessage());
            throw ApiException.serverError("Failed to read file.");
        }
    }

    public String getContentType(String relativePath) {
        if (relativePath == null) return "application/octet-stream";
        String lower = relativePath.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".mp4"))  return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".ogg"))  return "video/ogg";
        return "application/octet-stream";
    }

    private String getExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }
        return switch (contentType != null ? contentType : "") {
            case "image/jpeg"  -> "jpg";
            case "image/png"   -> "png";
            case "image/webp"  -> "webp";
            case "image/gif"   -> "gif";
            case "video/mp4"   -> "mp4";
            case "video/webm"  -> "webm";
            case "video/ogg"   -> "ogg";
            default            -> "bin";
        };
    }
}
