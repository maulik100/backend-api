package com.chehartemple.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GoogleDriveMediaUtil {

    private static final Pattern FILE_ID_PATTERN =
            Pattern.compile("(?:drive\\.google\\.com/(?:file/d/|open\\?id=)|docs\\.google\\.com/(?:uc\\?id=|uc\\?export=download&id=))([a-zA-Z0-9_-]{25,})");

    private GoogleDriveMediaUtil() {}

    public static boolean isGoogleDriveUrl(String url) {
        if (url == null || url.isBlank()) return false;
        return url.contains("drive.google.com") || url.contains("docs.google.com");
    }

    public static String extractFileId(String url) {
        if (url == null) return null;
        Matcher m = FILE_ID_PATTERN.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    /**
     * IMAGE: https://drive.google.com/uc?export=view&id=FILE_ID
     * VIDEO: https://drive.google.com/file/d/FILE_ID/preview
     */
    public static String toPreviewUrl(String url, boolean isVideo) {
        if (!isGoogleDriveUrl(url)) return url;
        String fileId = extractFileId(url);
        if (fileId == null) return url;
        return isVideo
                ? "https://drive.google.com/file/d/" + fileId + "/preview"
                : "https://drive.google.com/uc?export=view&id=" + fileId;
    }

    public static void validateGoogleDriveUrl(String url) {
        if (url == null || url.isBlank()) return;
        if (isGoogleDriveUrl(url) && extractFileId(url) == null) {
            throw new IllegalArgumentException("Invalid Google Drive URL format: " + url);
        }
    }
}
