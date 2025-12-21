package com.sms.grade.service;

import com.sms.grade.dto.CreateSubjectRequest;
import com.sms.grade.dto.SubjectResponse;
import com.sms.grade.dto.UpdateSubjectRequest;
import com.sms.grade.model.Subject;
import com.sms.grade.repository.SubjectRepository;
import com.sms.grade.service.interfaces.ISubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for subject reference data operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectService implements ISubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public List<SubjectResponse> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAllByOrderByDisplayOrderAsc();
        return subjects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectResponse getSubject(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        return mapToResponse(subject);
    }

    @Override
    public SubjectResponse getSubjectByCode(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        return mapToResponse(subject);
    }

    @Override
    public List<SubjectResponse> getCoreSubjects() {
        List<Subject> subjects = subjectRepository.findByIsCoreTrue();
        return subjects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectResponse> getSubjectsForGrade(Integer gradeLevel) {
        List<Subject> allSubjects = subjectRepository.findAllByOrderByDisplayOrderAsc();
        return allSubjects.stream()
                .filter(s -> s.getGradeLevels() != null && s.getGradeLevels().contains(gradeLevel))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(UUID id, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        subject.setName(request.getName());
        subject.setNameKhmer(request.getNameKhmer());
        subject.setCode(request.getCode());
        subject.setDescription(request.getDescription());
        subject.setDisplayOrder(request.getDisplayOrder());
        subject.setGradeLevels(request.getGradeLevels());

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Updated subject: {}", savedSubject.getId());

        return mapToResponse(savedSubject);
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        // Check if code already exists
        if (subjectRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Subject with code " + request.getCode() + " already exists");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .nameKhmer(request.getNameKhmer())
                .code(request.getCode())
                .description(request.getDescription())
                .isCore(request.getIsCore())
                .displayOrder(request.getDisplayOrder())
                .gradeLevels(request.getGradeLevels())
                .build();

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Created subject: {}", savedSubject.getId());

        return mapToResponse(savedSubject);
    }

    private SubjectResponse mapToResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .nameKhmer(subject.getNameKhmer())
                .code(subject.getCode())
                .gradeLevels(subject.getGradeLevels())
                .isCore(subject.getIsCore())
                .displayOrder(subject.getDisplayOrder())
                .build();
    }
}
