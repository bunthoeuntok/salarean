package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response for class list.
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassListResponse {

    private List<ClassSummaryDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
