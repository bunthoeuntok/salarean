package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer age;
    private UUID currentClassId;
    private String primaryParentContact;
}
