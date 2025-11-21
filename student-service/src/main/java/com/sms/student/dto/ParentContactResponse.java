package com.sms.student.dto;

import com.sms.student.enums.Relationship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentContactResponse {

    private UUID id;
    private String fullName;
    private String phoneNumber;
    private Relationship relationship;
    private Boolean isPrimary;
}
