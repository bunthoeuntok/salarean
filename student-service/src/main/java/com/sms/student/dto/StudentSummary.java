package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSummary {

    private UUID id;
    private String studentCode;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private LocalDate dateOfBirth;
    private Integer age;
    private UUID currentClassId;
    private String currentClassName;
    private String primaryParentContact;
    private String gender;
    private String status;
}
