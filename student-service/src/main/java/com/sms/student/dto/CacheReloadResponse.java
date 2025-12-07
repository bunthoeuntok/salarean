package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for cache reload operations.
 * Indicates successful cache eviction for the authenticated teacher.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheReloadResponse {

    /**
     * Success message indicating cache was cleared
     */
    private String message;

    /**
     * Teacher ID whose cache was cleared
     */
    private String teacherId;

    /**
     * Timestamp of the cache reload operation
     */
    private LocalDateTime reloadedAt;

    /**
     * Number of cache entries evicted (if available)
     */
    private Integer entriesCleared;
}
