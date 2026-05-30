package com.chehartemple.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InstagramDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaResponse {
        private UUID id;
        private String instagramMediaId;
        private String caption;
        private String mediaType;
        private String mediaUrl;
        private String thumbnailUrl;
        private String permalink;
        private String username;
        private LocalDateTime timestamp;
        private LocalDate postedDate;
        private Long likeCount;
        private Long commentsCount;
        private String mediaProductType;
        private boolean addedToApp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagedResponse {
        private List<MediaResponse> items;
        private int currentPage;
        private int totalPages;
        private long totalItems;
        private boolean hasNext;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateGroupedResponse {
        private Map<String, List<MediaResponse>> groupedByDate;
        private int daysOffset;
        private int daysCount;
        private boolean hasMore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> ok(String message, T data) {
            return ApiResponse.<T>builder().success(true).message(message).data(data).build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder().success(false).message(message).build();
        }
    }

    // Facebook Graph API response mapping
    @Data
    public static class GraphApiMediaResponse {
        private List<GraphApiMedia> data;
        private GraphApiPaging paging;
    }

    @Data
    public static class GraphApiMedia {
        private String id;
        private String caption;
        private String media_type;
        private String media_url;
        private String thumbnail_url;
        private String permalink;
        private String username;
        private String timestamp;
    }

    @Data
    public static class GraphApiPaging {
        private GraphApiCursors cursors;
        private String next;
    }

    @Data
    public static class GraphApiCursors {
        private String before;
        private String after;
    }
}
