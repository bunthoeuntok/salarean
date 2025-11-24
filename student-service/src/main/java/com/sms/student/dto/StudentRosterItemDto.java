package com.sms.student.dto;

import com.sms.student.enums.Gender;
import com.sms.student.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO representing a student in a class roster view.
 *
 * <p>Contains essential student information for displaying in class lists.
 * Lightweight version of full StudentResponse for roster displays.</p>
 *
 * @author SMS Development Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRosterItemDto {

    /**
     * Unique identifier for the student.
     */
    private UUID studentId;

    /**
     * Student code/ID number (e.g., "STU2024001").
     */
    private String studentCode;

    /**
     * Student's first name (Latin script).
     */
    private String firstName;

    /**
     * Student's last name (Latin script).
     */
    private String lastName;

    /**
     * Student's first name in Khmer script.
     */
    private String firstNameKhmer;

    /**
     * Student's last name in Khmer script.
     */
    private String lastNameKhmer;

    /**
     * Student's gender.
     */
    private Gender gender;

    /**
     * Student's photo URL.
     */
    private String photoUrl;

    /**
     * Date when the student enrolled in this class.
     */
    private LocalDate enrollmentDate;

    /**
     * Current status of the student.
     */
    private StudentStatus status;
}
