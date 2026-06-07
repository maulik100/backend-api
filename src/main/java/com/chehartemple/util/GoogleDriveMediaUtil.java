package com.chehartemple.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GoogleDriveMediaUtil {

    // Matches: /file/d/FILE_ID, open?id=FILE_ID, uc?id=FILE_ID, uc?export=...&id=FILE_ID
    private static final Pattern FILE_ID_PATTERN = Pattern.compile(
            "drive\\.google\\.com/file/d/([a-zA-Z0-9_-]{10,})"
            + "|drive\\.google\\.com/open\\?id=([a-zA-Z0-9_-]{10,})"
            + "|drive\\.google\\.com/uc\\?(?:[^&]*&)*id=([a-zA-Z0-9_-]{10,})"
            + "|docs\\.google\\.com/uc\\?(?:[^&]*&)*id=([a-zA-Z0-9_-]{10,})"
    );

    // Matches: /drive/folders/FOLDER_ID  or  /drive/folders/FOLDER_ID?...
    private static final Pattern FOLDER_ID_PATTERN = Pattern.compile(
            "drive\\.google\\.com/drive/folders/([a-zA-Z0-9_-]{10,})"
    );

    private GoogleDriveMediaUtil() {}

    public static boolean isGoogleDriveUrl(String url) {
        if (url == null || url.isBlank()) return false;
        return url.contains("drive.google.com") || url.contains("docs.google.com");
    }

    public static boolean isFolderUrl(String url) {
        return url != null && url.contains("drive.google.com/drive/folders");
    }

    /**
     * Extracts the file or folder ID from any supported Google Drive URL.
     */
    public static String extractFileId(String url) {
        if (url == null) return null;
        // Try file pattern first
        Matcher m = FILE_ID_PATTERN.matcher(url);
        if (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                if (m.group(i) != null) return m.group(i);
            }
        }
        // Fall back to folder pattern
        Matcher fm = FOLDER_ID_PATTERN.matcher(url);
        return fm.find() ? fm.group(1) : null;
    }

    /**
     * Converts any supported Google Drive URL to a previewable/embeddable URL.
     *
     * File  IMAGE: https://drive.google.com/uc?export=view&id=FILE_ID
     * File  VIDEO: https://drive.google.com/file/d/FILE_ID/preview
     * Folder     : https://drive.google.com/embeddedfolderview?id=FOLDER_ID#list
     */
    public static String toPreviewUrl(String url, boolean isVideo) {
        if (!isGoogleDriveUrl(url)) return url;
        String id = extractFileId(url);
        if (id == null) return url;
        if (isFolderUrl(url)) {
            return "https://drive.google.com/embeddedfolderview?id=" + id + "#list";
        }
        return isVideo
                ? "https://drive.google.com/file/d/" + id + "/preview"
                : "https://drive.google.com/uc?export=view&id=" + id;
    }

    /**
     * Validates that a Google Drive URL is parseable.
     * Accepts file links, open?id= links, uc?id= links, AND folder links.
     * Rejects Drive URLs where no ID can be extracted at all.
     */
    public static void validateGoogleDriveUrl(String url) {
        if (url == null || url.isBlank()) return;
        if (!isGoogleDriveUrl(url)) return; // non-Drive URLs allowed as-is
        if (extractFileId(url) == null) {
            throw com.chehartemple.exception.ApiException.badRequest(
                    "Could not extract a file/folder ID from the Google Drive URL you provided. "
                    + "Please use one of these supported formats:\n"
                    + "  • https://drive.google.com/file/d/FILE_ID/view  (single file)\n"
                    + "  • https://drive.google.com/open?id=FILE_ID  (single file)\n"
                    + "  • https://drive.google.com/drive/folders/FOLDER_ID  (folder)\n"
                    + "To get a file link: open the file in Google Drive → right-click → \"Get link\" → Copy."
            );
        }
    }
}
