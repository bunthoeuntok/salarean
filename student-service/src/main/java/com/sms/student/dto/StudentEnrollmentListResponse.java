package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO containing all students enrolled in a class.
 *
 * <p>Wraps the student list with a total count for frontend display.
 * No pagination - all students returned in a single response.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEnrollmentListResponse {

    /**
     * List of all student enrollment items matching the filter criteria.
     */
    private List<StudentEnrollmentItem> students;

    /**
     * Total number of students returned.
     */
    private int totalCount;
}
