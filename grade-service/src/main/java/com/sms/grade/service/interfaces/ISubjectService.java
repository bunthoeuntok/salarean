package com.sms.grade.service.interfaces;

import com.sms.grade.dto.CreateSubjectRequest;
import com.sms.grade.dto.SubjectResponse;
import com.sms.grade.dto.UpdateSubjectRequest;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for subject reference data operations.
 */
public interface ISubjectService {

    /**
     * Get all subjects ordered by display order.
     */
    List<SubjectResponse> getAllSubjects();

    /**
     * Get a subject by ID.
     */
    SubjectResponse getSubject(UUID id);

    /**
     * Get a subject by code.
     */
    SubjectResponse getSubjectByCode(String code);

    /**
     * Get all core subjects.
     */
    List<SubjectResponse> getCoreSubjects();

    /**
     * Get subjects for a specific grade level.
     */
    List<SubjectResponse> getSubjectsForGrade(Integer gradeLevel);

    /**
     * Update a subject.
     */
    SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request);

    /**
     * Create a new subject.
     */
    SubjectResponse createSubject(CreateSubjectRequest request);
}
