package com.sms.grade.service;

import com.sms.grade.dto.*;
import com.sms.grade.enums.AssessmentCategory;
import com.sms.grade.exception.*;
import com.sms.grade.model.AssessmentType;
import com.sms.grade.model.Grade;
import com.sms.grade.model.Subject;
import com.sms.grade.repository.AssessmentTypeRepository;
import com.sms.grade.repository.GradeRepository;
import com.sms.grade.repository.SubjectRepository;
import com.sms.grade.security.TeacherContextHolder;
import com.sms.grade.service.interfaces.ICalculationService;
import com.sms.grade.service.interfaces.IGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for grade CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService implements IGradeService {

    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final AssessmentTypeRepository assessmentTypeRepository;
    private final ICalculationService calculationService;

    @Override
    @Transactional
    public GradeResponse createGrade(GradeRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Creating grade for student {} subject {} by teacher {}",
                request.getStudentId(), request.getSubjectId(), teacherId);

        // Check for duplicate
        if (gradeRepository.existsByStudentIdAndClassIdAndSubjectIdAndAssessmentTypeIdAndSemesterAndAcademicYear(
                request.getStudentId(), request.getClassId(), request.getSubjectId(),
                request.getAssessmentTypeId(), request.getSemester(), request.getAcademicYear())) {
            throw new DuplicateGradeException("Grade entry already exists for this student/assessment");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException(request.getSubjectId()));

        AssessmentType assessmentType = assessmentTypeRepository.findById(request.getAssessmentTypeId())
                .orElseThrow(() -> new AssessmentTypeNotFoundException(request.getAssessmentTypeId()));

        // Validate score
        if (request.getScore().compareTo(assessmentType.getMaxScore()) > 0) {
            throw new InvalidGradeDataException("Score exceeds maximum of " + assessmentType.getMaxScore());
        }

        Grade grade = new Grade();
        grade.setTeacherId(teacherId);
        grade.setStudentId(request.getStudentId());
        grade.setClassId(request.getClassId());
        grade.setSubject(subject);
        grade.setAssessmentType(assessmentType);
        grade.setScore(request.getScore());
        grade.setSemester(request.getSemester());
        grade.setAcademicYear(request.getAcademicYear());
        grade.setComments(request.getComments());

        grade = gradeRepository.save(grade);
        log.info("Created grade {}", grade.getId());

        // Trigger recalculation
        calculationService.recalculateOnGradeChange(
                request.getStudentId(), request.getClassId(), request.getSubjectId(),
                request.getSemester(), request.getAcademicYear());

        return mapToResponse(grade);
    }

    @Override
    @Transactional
    public List<GradeResponse> createBulkGrades(BulkGradeRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Creating bulk grades for class {} subject {}", request.getClassId(), request.getSubjectId());

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException(request.getSubjectId()));

        AssessmentType assessmentType = assessmentTypeRepository.findById(request.getAssessmentTypeId())
                .orElseThrow(() -> new AssessmentTypeNotFoundException(request.getAssessmentTypeId()));

        List<Grade> grades = new ArrayList<>();

        for (BulkGradeRequest.StudentGradeEntry entry : request.getGrades()) {
            // Skip if already exists
            if (gradeRepository.existsByStudentIdAndClassIdAndSubjectIdAndAssessmentTypeIdAndSemesterAndAcademicYear(
                    entry.getStudentId(), request.getClassId(), request.getSubjectId(),
                    request.getAssessmentTypeId(), request.getSemester(), request.getAcademicYear())) {
                log.warn("Skipping duplicate grade for student {}", entry.getStudentId());
                continue;
            }

            Grade grade = new Grade();
            grade.setTeacherId(teacherId);
            grade.setStudentId(entry.getStudentId());
            grade.setClassId(request.getClassId());
            grade.setSubject(subject);
            grade.setAssessmentType(assessmentType);
            grade.setScore(entry.getScore());
            grade.setSemester(request.getSemester());
            grade.setAcademicYear(request.getAcademicYear());
            grade.setComments(entry.getComments());

            grades.add(grade);
        }

        grades = gradeRepository.saveAll(grades);
        log.info("Created {} bulk grades", grades.size());

        return grades.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GradeResponse updateGrade(UUID gradeId, GradeRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Updating grade {} by teacher {}", gradeId, teacherId);

        Grade grade = gradeRepository.findByIdAndTeacherId(gradeId, teacherId)
                .orElseThrow(() -> new UnauthorizedAccessException("Grade not found or not authorized"));

        AssessmentType assessmentType = assessmentTypeRepository.findById(request.getAssessmentTypeId())
                .orElseThrow(() -> new AssessmentTypeNotFoundException(request.getAssessmentTypeId()));

        // Validate score
        if (request.getScore().compareTo(assessmentType.getMaxScore()) > 0) {
            throw new InvalidGradeDataException("Score exceeds maximum of " + assessmentType.getMaxScore());
        }

        grade.setScore(request.getScore());
        grade.setComments(request.getComments());

        grade = gradeRepository.save(grade);
        log.info("Updated grade {}", gradeId);

        // Trigger recalculation
        calculationService.recalculateOnGradeChange(
                grade.getStudentId(), grade.getClassId(), grade.getSubject().getId(),
                grade.getSemester(), grade.getAcademicYear());

        return mapToResponse(grade);
    }

    @Override
    @Transactional
    public void deleteGrade(UUID gradeId) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Deleting grade {} by teacher {}", gradeId, teacherId);

        Grade grade = gradeRepository.findByIdAndTeacherId(gradeId, teacherId)
                .orElseThrow(() -> new UnauthorizedAccessException("Grade not found or not authorized"));

        UUID studentId = grade.getStudentId();
        UUID classId = grade.getClassId();
        UUID subjectId = grade.getSubject().getId();
        Integer semester = grade.getSemester();
        String academicYear = grade.getAcademicYear();

        gradeRepository.delete(grade);
        log.info("Deleted grade {}", gradeId);

        // Trigger recalculation
        calculationService.recalculateOnGradeChange(studentId, classId, subjectId, semester, academicYear);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse getGrade(UUID gradeId) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        Grade grade = gradeRepository.findByIdAndTeacherId(gradeId, teacherId)
                .orElseThrow(() -> new UnauthorizedAccessException("Grade not found or not authorized"));

        return mapToResponse(grade);
    }

    @Override
    @Transactional
    public List<GradeResponse> enterMonthlyGrades(MonthlyGradeEntryRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Entering monthly grades for class {} subject {}", request.getClassId(), request.getSubjectId());

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException(request.getSubjectId()));

        // Get monthly exam assessment types
        List<AssessmentType> monthlyExamTypes = assessmentTypeRepository
                .findByCategoryOrderByDisplayOrderAsc(AssessmentCategory.MONTHLY_EXAM);

        List<Grade> allGrades = new ArrayList<>();

        for (MonthlyGradeEntryRequest.StudentMonthlyGrades studentGrades : request.getStudentGrades()) {
            BigDecimal[] scores = {
                    studentGrades.getExam1Score(),
                    studentGrades.getExam2Score(),
                    studentGrades.getExam3Score(),
                    studentGrades.getExam4Score()
            };

            for (int i = 0; i < scores.length && i < monthlyExamTypes.size(); i++) {
                if (scores[i] == null) continue;

                AssessmentType assessmentType = monthlyExamTypes.get(i);

                // Check if already exists
                if (gradeRepository.existsByStudentIdAndClassIdAndSubjectIdAndAssessmentTypeIdAndSemesterAndAcademicYear(
                        studentGrades.getStudentId(), request.getClassId(), request.getSubjectId(),
                        assessmentType.getId(), request.getSemester(), request.getAcademicYear())) {
                    // Update existing
                    List<Grade> existing = gradeRepository.findByTeacherIdAndStudentIdAndSubjectIdAndAcademicYear(
                            teacherId, studentGrades.getStudentId(), request.getSubjectId(), request.getAcademicYear());

                    for (Grade g : existing) {
                        if (g.getAssessmentType().getId().equals(assessmentType.getId()) &&
                                g.getSemester().equals(request.getSemester())) {
                            g.setScore(scores[i]);
                            g.setComments(studentGrades.getComments());
                            allGrades.add(gradeRepository.save(g));
                            break;
                        }
                    }
                } else {
                    // Create new
                    Grade grade = new Grade();
                    grade.setTeacherId(teacherId);
                    grade.setStudentId(studentGrades.getStudentId());
                    grade.setClassId(request.getClassId());
                    grade.setSubject(subject);
                    grade.setAssessmentType(assessmentType);
                    grade.setScore(scores[i]);
                    grade.setSemester(request.getSemester());
                    grade.setAcademicYear(request.getAcademicYear());
                    grade.setComments(studentGrades.getComments());
                    allGrades.add(gradeRepository.save(grade));
                }
            }
        }

        log.info("Entered {} monthly grades", allGrades.size());
        return allGrades.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getStudentMonthlyGrades(UUID studentId, UUID subjectId,
                                                        Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        List<Grade> grades = gradeRepository.findStudentMonthlyExams(
                teacherId, studentId, subjectId, semester, academicYear);

        return grades.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GradeResponse> enterSemesterExamGrades(BulkGradeRequest request) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Entering semester exam grades for class {} subject {}", request.getClassId(), request.getSubjectId());

        // Get semester exam assessment type
        AssessmentType semesterExamType = assessmentTypeRepository.findByCode("SEM_EXAM")
                .orElseThrow(() -> new AssessmentTypeNotFoundException("SEM_EXAM"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException(request.getSubjectId()));

        List<Grade> grades = new ArrayList<>();

        for (BulkGradeRequest.StudentGradeEntry entry : request.getGrades()) {
            // Check if already exists - update if so
            Optional<Grade> existing = gradeRepository.findStudentSemesterExam(
                    teacherId, entry.getStudentId(), request.getSubjectId(),
                    request.getSemester(), request.getAcademicYear());

            Grade grade;
            if (existing.isPresent()) {
                grade = existing.get();
                grade.setScore(entry.getScore());
                grade.setComments(entry.getComments());
            } else {
                grade = new Grade();
                grade.setTeacherId(teacherId);
                grade.setStudentId(entry.getStudentId());
                grade.setClassId(request.getClassId());
                grade.setSubject(subject);
                grade.setAssessmentType(semesterExamType);
                grade.setScore(entry.getScore());
                grade.setSemester(request.getSemester());
                grade.setAcademicYear(request.getAcademicYear());
                grade.setComments(entry.getComments());
            }

            grades.add(gradeRepository.save(grade));
        }

        log.info("Entered {} semester exam grades", grades.size());
        return grades.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse getStudentSemesterExam(UUID studentId, UUID subjectId,
                                                 Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        Grade grade = gradeRepository.findStudentSemesterExam(
                        teacherId, studentId, subjectId, semester, academicYear)
                .orElseThrow(() -> new GradeNotFoundException("Semester exam grade not found"));

        return mapToResponse(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentGradesSummary getStudentSemesterGrades(UUID studentId, Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        List<Grade> grades = gradeRepository.findByTeacherIdAndStudentIdAndSemesterAndAcademicYear(
                teacherId, studentId, semester, academicYear);

        if (grades.isEmpty()) {
            throw new GradeNotFoundException("No grades found for student");
        }

        UUID classId = grades.get(0).getClassId();

        // Group by subject
        Map<UUID, List<Grade>> gradesBySubject = grades.stream()
                .collect(Collectors.groupingBy(g -> g.getSubject().getId()));

        List<StudentGradesSummary.SubjectGrades> subjectGradesList = new ArrayList<>();

        for (Map.Entry<UUID, List<Grade>> entry : gradesBySubject.entrySet()) {
            List<Grade> subjectGrades = entry.getValue();
            Subject subject = subjectGrades.get(0).getSubject();

            List<StudentGradesSummary.GradeEntry> monthlyExams = subjectGrades.stream()
                    .filter(g -> g.getAssessmentType().getCategory() == AssessmentCategory.MONTHLY_EXAM)
                    .sorted(Comparator.comparingInt(g -> g.getAssessmentType().getDisplayOrder()))
                    .map(g -> StudentGradesSummary.GradeEntry.builder()
                            .gradeId(g.getId())
                            .assessmentName(g.getAssessmentType().getName())
                            .assessmentCode(g.getAssessmentType().getCode())
                            .score(g.getScore())
                            .maxScore(g.getAssessmentType().getMaxScore())
                            .percentage(g.getPercentage())
                            .build())
                    .collect(Collectors.toList());

            StudentGradesSummary.GradeEntry semesterExam = subjectGrades.stream()
                    .filter(g -> g.getAssessmentType().getCategory() == AssessmentCategory.SEMESTER_EXAM)
                    .findFirst()
                    .map(g -> StudentGradesSummary.GradeEntry.builder()
                            .gradeId(g.getId())
                            .assessmentName(g.getAssessmentType().getName())
                            .assessmentCode(g.getAssessmentType().getCode())
                            .score(g.getScore())
                            .maxScore(g.getAssessmentType().getMaxScore())
                            .percentage(g.getPercentage())
                            .build())
                    .orElse(null);

            // Calculate averages
            BigDecimal monthlyAvg = null;
            if (!monthlyExams.isEmpty()) {
                BigDecimal sum = monthlyExams.stream()
                        .map(StudentGradesSummary.GradeEntry::getScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                monthlyAvg = sum.divide(BigDecimal.valueOf(monthlyExams.size()), 2, java.math.RoundingMode.HALF_UP);
            }

            subjectGradesList.add(StudentGradesSummary.SubjectGrades.builder()
                    .subjectId(subject.getId())
                    .subjectName(subject.getName())
                    .subjectNameKhmer(subject.getNameKhmer())
                    .isCore(subject.getIsCore())
                    .monthlyExams(monthlyExams)
                    .semesterExam(semesterExam)
                    .monthlyAverage(monthlyAvg)
                    .build());
        }

        return StudentGradesSummary.builder()
                .studentId(studentId)
                .classId(classId)
                .semester(semester)
                .academicYear(academicYear)
                .subjectGrades(subjectGradesList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassGradesSummary getClassGrades(UUID classId, Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        List<Grade> grades = gradeRepository.findByTeacherIdAndClassIdAndSemesterAndAcademicYear(
                teacherId, classId, semester, academicYear);

        if (grades.isEmpty()) {
            return ClassGradesSummary.builder()
                    .classId(classId)
                    .semester(semester)
                    .academicYear(academicYear)
                    .totalStudents(0)
                    .students(Collections.emptyList())
                    .build();
        }

        // Count unique students
        Set<UUID> studentIds = grades.stream()
                .map(Grade::getStudentId)
                .collect(Collectors.toSet());

        return ClassGradesSummary.builder()
                .classId(classId)
                .semester(semester)
                .academicYear(academicYear)
                .totalStudents(studentIds.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getClassSubjectGrades(UUID classId, UUID subjectId,
                                                      Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();

        List<Grade> grades = gradeRepository.findByTeacherIdAndClassIdAndSubjectIdAndSemesterAndAcademicYear(
                teacherId, classId, subjectId, semester, academicYear);

        return grades.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private GradeResponse mapToResponse(Grade grade) {
        return GradeResponse.builder()
                .id(grade.getId())
                .studentId(grade.getStudentId())
                .classId(grade.getClassId())
                .subjectId(grade.getSubject().getId())
                .subjectName(grade.getSubject().getName())
                .subjectNameKhmer(grade.getSubject().getNameKhmer())
                .assessmentTypeId(grade.getAssessmentType().getId())
                .assessmentTypeName(grade.getAssessmentType().getName())
                .assessmentTypeCode(grade.getAssessmentType().getCode())
                .score(grade.getScore())
                .maxScore(grade.getAssessmentType().getMaxScore())
                .percentage(grade.getPercentage())
                .letterGrade(grade.getLetterGrade())
                .semester(grade.getSemester())
                .academicYear(grade.getAcademicYear())
                .comments(grade.getComments())
                .createdAt(grade.getCreatedAt())
                .updatedAt(grade.getUpdatedAt())
                .build();
    }
}
