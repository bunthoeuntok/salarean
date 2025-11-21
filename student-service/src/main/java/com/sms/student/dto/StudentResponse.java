package com.sms.student.dto;

import com.sms.student.enums.Gender;
import com.sms.student.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private UUID id;
    private String studentCode;
    private String firstName;
    private String lastName;
    private String firstNameKhmer;
    private String lastNameKhmer;
    private LocalDate dateOfBirth;
    private Integer age;
    private Gender gender;
    private String photoUrl;
    private String address;
    private String emergencyContact;
    private LocalDate enrollmentDate;
    private StudentStatus status;
    private UUID currentClassId;
    private List<ParentContactResponse> parentContacts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
