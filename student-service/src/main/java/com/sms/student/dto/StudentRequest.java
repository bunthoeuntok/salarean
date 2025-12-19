package com.sms.student.dto;

import com.sms.student.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Size(max = 100, message = "First name (Khmer) must not exceed 100 characters")
    private String firstNameKhmer;

    @Size(max = 100, message = "Last name (Khmer) must not exceed 100 characters")
    private String lastNameKhmer;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private UUID classId;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 20, message = "Emergency contact must not exceed 20 characters")
    private String emergencyContact;

    private LocalDate enrollmentDate;

    @Valid
    private List<ParentContactRequest> parentContacts;
}
