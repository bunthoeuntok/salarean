package com.sms.grade.service;

import com.sms.grade.dto.CalculationResult;
import com.sms.grade.dto.RankingResponse;
import com.sms.grade.dto.TeacherAssessmentConfigResponse;
import com.sms.grade.enums.AssessmentCategory;
import com.sms.grade.enums.AverageType;
import com.sms.grade.exception.CalculationException;
import com.sms.grade.exception.InsufficientGradesException;
import com.sms.grade.model.Grade;
import com.sms.grade.model.GradeAverage;
import com.sms.grade.model.Subject;
import com.sms.grade.repository.GradeAverageRepository;
import com.sms.grade.repository.GradeRepository;
import com.sms.grade.repository.SubjectRepository;
import com.sms.grade.security.TeacherContextHolder;
import com.sms.grade.service.interfaces.ICalculationService;
import com.sms.grade.service.interfaces.IConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for grade calculations following MoEYS standards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationService implements ICalculationService {

    private final GradeRepository gradeRepository;
    private final GradeAverageRepository gradeAverageRepository;
    private final SubjectRepository subjectRepository;
    private final IConfigurationService configurationService;

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    @Transactional
    public CalculationResult calculateMonthlyAverage(UUID studentId, UUID subjectId,
                                                      Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Calculating monthly average for student {} subject {} semester {}",
                studentId, subjectId, semester);

        // Get monthly exam grades
        List<Grade> monthlyGrades = gradeRepository.findStudentMonthlyExams(
                teacherId, studentId, subjectId, semester, academicYear);

        if (monthlyGrades.isEmpty()) {
            throw new InsufficientGradesException("No monthly exam grades found");
        }

        // Get config for expected exam count
        TeacherAssessmentConfigResponse config = configurationService.getConfig(
                monthlyGrades.get(0).getClassId(), subjectId, semester, academicYear);

        // Calculate average
        BigDecimal sum = monthlyGrades.stream()
                .map(Grade::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(BigDecimal.valueOf(monthlyGrades.size()), SCALE, ROUNDING_MODE);

        // Build result with details
        List<CalculationResult.GradeComponent> components = monthlyGrades.stream()
                .map(g -> CalculationResult.GradeComponent.builder()
                        .name(g.getAssessmentType().getName())
                        .score(g.getScore())
                        .maxScore(g.getAssessmentType().getMaxScore())
                        .percentage(g.getPercentage())
                        .build())
                .collect(Collectors.toList());

        Subject subject = monthlyGrades.get(0).getSubject();

        return CalculationResult.builder()
                .studentId(studentId)
                .classId(monthlyGrades.get(0).getClassId())
                .subjectId(subjectId)
                .subjectName(subject.getName())
                .semester(semester)
                .academicYear(academicYear)
                .averageType(AverageType.MONTHLY_AVERAGE)
                .calculatedScore(average)
                .letterGrade(getLetterGrade(average.doubleValue()))
                .details(CalculationResult.CalculationDetails.builder()
                        .monthlyExams(components)
                        .monthlyAverage(average)
                        .formula("(" + monthlyGrades.stream()
                                .map(g -> g.getScore().toString())
                                .collect(Collectors.joining(" + ")) +
                                ") / " + monthlyGrades.size())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public CalculationResult calculateSubjectSemesterAverage(UUID studentId, UUID subjectId,
                                                              Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Calculating semester average for student {} subject {}", studentId, subjectId);

        // Get monthly average
        CalculationResult monthlyResult = calculateMonthlyAverage(studentId, subjectId, semester, academicYear);
        BigDecimal monthlyAvg = monthlyResult.getCalculatedScore();

        // Get semester exam
        Grade semesterExam = gradeRepository.findStudentSemesterExam(
                        teacherId, studentId, subjectId, semester, academicYear)
                .orElseThrow(() -> new InsufficientGradesException("Semester exam grade not found"));

        // Get config for weights
        TeacherAssessmentConfigResponse config = configurationService.getConfig(
                semesterExam.getClassId(), subjectId, semester, academicYear);

        BigDecimal monthlyWeight = config.getMonthlyWeight().divide(BigDecimal.valueOf(100), 4, ROUNDING_MODE);
        BigDecimal semesterWeight = config.getSemesterExamWeight().divide(BigDecimal.valueOf(100), 4, ROUNDING_MODE);

        // Calculate weighted average
        BigDecimal weightedMonthly = monthlyAvg.multiply(monthlyWeight);
        BigDecimal weightedSemester = semesterExam.getScore().multiply(semesterWeight);
        BigDecimal semesterAverage = weightedMonthly.add(weightedSemester).setScale(SCALE, ROUNDING_MODE);

        // Save to grade_averages table
        saveOrUpdateAverage(teacherId, studentId, semesterExam.getClassId(), subjectId,
                semester, academicYear, AverageType.SEMESTER_AVERAGE, semesterAverage);

        return CalculationResult.builder()
                .studentId(studentId)
                .classId(semesterExam.getClassId())
                .subjectId(subjectId)
                .subjectName(semesterExam.getSubject().getName())
                .semester(semester)
                .academicYear(academicYear)
                .averageType(AverageType.SEMESTER_AVERAGE)
                .calculatedScore(semesterAverage)
                .letterGrade(getLetterGrade(semesterAverage.doubleValue()))
                .details(CalculationResult.CalculationDetails.builder()
                        .monthlyExams(monthlyResult.getDetails().getMonthlyExams())
                        .semesterExam(CalculationResult.GradeComponent.builder()
                                .name(semesterExam.getAssessmentType().getName())
                                .score(semesterExam.getScore())
                                .maxScore(semesterExam.getAssessmentType().getMaxScore())
                                .percentage(semesterExam.getPercentage())
                                .build())
                        .monthlyWeight(config.getMonthlyWeight())
                        .semesterWeight(config.getSemesterExamWeight())
                        .monthlyAverage(monthlyAvg)
                        .weightedMonthly(weightedMonthly.setScale(SCALE, ROUNDING_MODE))
                        .weightedSemester(weightedSemester.setScale(SCALE, ROUNDING_MODE))
                        .formula(String.format("(%s × %s%%) + (%s × %s%%)",
                                monthlyAvg, config.getMonthlyWeight(),
                                semesterExam.getScore(), config.getSemesterExamWeight()))
                        .build())
                .build();
    }

    @Override
    @Transactional
    public CalculationResult calculateOverallSemesterAverage(UUID studentId, Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Calculating overall semester average for student {}", studentId);

        // Get all semester grades for the student
        List<Grade> semesterGrades = gradeRepository.findByTeacherIdAndStudentIdAndSemesterAndAcademicYear(
                teacherId, studentId, semester, academicYear);

        if (semesterGrades.isEmpty()) {
            throw new InsufficientGradesException("No grades found for semester");
        }

        UUID classId = semesterGrades.get(0).getClassId();

        // Get unique subjects
        Set<UUID> subjectIds = semesterGrades.stream()
                .map(g -> g.getSubject().getId())
                .collect(Collectors.toSet());

        // Calculate semester average for each subject
        List<BigDecimal> subjectAverages = new ArrayList<>();
        for (UUID subjectId : subjectIds) {
            try {
                CalculationResult result = calculateSubjectSemesterAverage(studentId, subjectId, semester, academicYear);
                subjectAverages.add(result.getCalculatedScore());
            } catch (InsufficientGradesException e) {
                log.warn("Skipping subject {} - insufficient grades", subjectId);
            }
        }

        if (subjectAverages.isEmpty()) {
            throw new CalculationException("No complete subject grades for overall calculation");
        }

        // Calculate overall average
        BigDecimal sum = subjectAverages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallAverage = sum.divide(BigDecimal.valueOf(subjectAverages.size()), SCALE, ROUNDING_MODE);

        // Save to grade_averages
        saveOrUpdateAverage(teacherId, studentId, classId, null,
                semester, academicYear, AverageType.OVERALL_SEMESTER, overallAverage);

        return CalculationResult.builder()
                .studentId(studentId)
                .classId(classId)
                .semester(semester)
                .academicYear(academicYear)
                .averageType(AverageType.OVERALL_SEMESTER)
                .calculatedScore(overallAverage)
                .letterGrade(getLetterGrade(overallAverage.doubleValue()))
                .build();
    }

    @Override
    @Transactional
    public CalculationResult calculateSubjectAnnualAverage(UUID studentId, UUID subjectId, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Calculating annual average for student {} subject {}", studentId, subjectId);

        // Calculate both semester averages
        CalculationResult sem1 = calculateSubjectSemesterAverage(studentId, subjectId, 1, academicYear);
        CalculationResult sem2 = calculateSubjectSemesterAverage(studentId, subjectId, 2, academicYear);

        // Annual = (Sem1 + Sem2) / 2
        BigDecimal annualAverage = sem1.getCalculatedScore()
                .add(sem2.getCalculatedScore())
                .divide(BigDecimal.valueOf(2), SCALE, ROUNDING_MODE);

        // Save to grade_averages
        saveOrUpdateAverage(teacherId, studentId, sem1.getClassId(), subjectId,
                null, academicYear, AverageType.SUBJECT_ANNUAL, annualAverage);

        return CalculationResult.builder()
                .studentId(studentId)
                .classId(sem1.getClassId())
                .subjectId(subjectId)
                .subjectName(sem1.getSubjectName())
                .academicYear(academicYear)
                .averageType(AverageType.SUBJECT_ANNUAL)
                .calculatedScore(annualAverage)
                .letterGrade(getLetterGrade(annualAverage.doubleValue()))
                .details(CalculationResult.CalculationDetails.builder()
                        .formula(String.format("(%s + %s) / 2",
                                sem1.getCalculatedScore(), sem2.getCalculatedScore()))
                        .build())
                .build();
    }

    @Override
    @Transactional
    public CalculationResult calculateOverallAnnualAverage(UUID studentId, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Calculating overall annual average for student {}", studentId);

        // Calculate both overall semester averages
        CalculationResult sem1 = calculateOverallSemesterAverage(studentId, 1, academicYear);
        CalculationResult sem2 = calculateOverallSemesterAverage(studentId, 2, academicYear);

        // Annual = (Sem1 + Sem2) / 2
        BigDecimal annualAverage = sem1.getCalculatedScore()
                .add(sem2.getCalculatedScore())
                .divide(BigDecimal.valueOf(2), SCALE, ROUNDING_MODE);

        // Save to grade_averages
        saveOrUpdateAverage(teacherId, studentId, sem1.getClassId(), null,
                null, academicYear, AverageType.OVERALL_ANNUAL, annualAverage);

        return CalculationResult.builder()
                .studentId(studentId)
                .classId(sem1.getClassId())
                .academicYear(academicYear)
                .averageType(AverageType.OVERALL_ANNUAL)
                .calculatedScore(annualAverage)
                .letterGrade(getLetterGrade(annualAverage.doubleValue()))
                .build();
    }

    @Override
    @Transactional
    public void calculateClassAverages(UUID classId, Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Calculating all averages for class {} semester {}", classId, semester);

        // Get all grades for the class
        List<Grade> classGrades = gradeRepository.findByTeacherIdAndClassIdAndSemesterAndAcademicYear(
                teacherId, classId, semester, academicYear);

        // Get unique students
        Set<UUID> studentIds = classGrades.stream()
                .map(Grade::getStudentId)
                .collect(Collectors.toSet());

        // Calculate for each student
        for (UUID studentId : studentIds) {
            try {
                calculateOverallSemesterAverage(studentId, semester, academicYear);
            } catch (Exception e) {
                log.warn("Error calculating average for student {}: {}", studentId, e.getMessage());
            }
        }

        // Calculate rankings
        calculateClassRankings(classId, semester, academicYear);
    }

    @Override
    @Transactional
    public RankingResponse calculateClassRankings(UUID classId, Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Calculating class rankings for class {} semester {}", classId, semester);

        // Get overall semester averages for all students
        List<GradeAverage> averages = gradeAverageRepository.findClassRankings(
                teacherId, classId, semester, academicYear, AverageType.OVERALL_SEMESTER);

        // Assign ranks
        List<RankingResponse.StudentRanking> rankings = new ArrayList<>();
        int rank = 1;
        BigDecimal previousScore = null;
        int sameRankCount = 0;

        for (GradeAverage avg : averages) {
            if (previousScore != null && avg.getAverageScore().compareTo(previousScore) != 0) {
                rank += sameRankCount;
                sameRankCount = 1;
            } else {
                sameRankCount++;
            }

            // Update rank in database
            avg.setClassRank(rank);
            gradeAverageRepository.save(avg);

            rankings.add(RankingResponse.StudentRanking.builder()
                    .rank(rank)
                    .studentId(avg.getStudentId())
                    .averageScore(avg.getAverageScore())
                    .letterGrade(avg.getLetterGrade())
                    .build());

            previousScore = avg.getAverageScore();
        }

        return RankingResponse.builder()
                .classId(classId)
                .semester(semester)
                .academicYear(academicYear)
                .totalStudents(rankings.size())
                .rankings(rankings)
                .build();
    }

    @Override
    @Transactional
    public RankingResponse calculateSubjectRankings(UUID classId, UUID subjectId,
                                                     Integer semester, String academicYear) {
        UUID teacherId = TeacherContextHolder.getTeacherId();
        log.info("Calculating subject rankings for class {} subject {}", classId, subjectId);

        List<GradeAverage> averages = gradeAverageRepository.findSubjectRankings(
                teacherId, classId, subjectId, semester, academicYear, AverageType.SEMESTER_AVERAGE);

        Subject subject = subjectRepository.findById(subjectId).orElse(null);

        List<RankingResponse.StudentRanking> rankings = new ArrayList<>();
        int rank = 1;
        BigDecimal previousScore = null;
        int sameRankCount = 0;

        for (GradeAverage avg : averages) {
            if (previousScore != null && avg.getAverageScore().compareTo(previousScore) != 0) {
                rank += sameRankCount;
                sameRankCount = 1;
            } else {
                sameRankCount++;
            }

            avg.setSubjectRank(rank);
            gradeAverageRepository.save(avg);

            rankings.add(RankingResponse.StudentRanking.builder()
                    .rank(rank)
                    .studentId(avg.getStudentId())
                    .averageScore(avg.getAverageScore())
                    .letterGrade(avg.getLetterGrade())
                    .build());

            previousScore = avg.getAverageScore();
        }

        return RankingResponse.builder()
                .classId(classId)
                .subjectId(subjectId)
                .subjectName(subject != null ? subject.getName() : null)
                .semester(semester)
                .academicYear(academicYear)
                .totalStudents(rankings.size())
                .rankings(rankings)
                .build();
    }

    @Override
    public String getLetterGrade(double percentage) {
        if (percentage >= 85) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 55) return "C";
        if (percentage >= 40) return "D";
        if (percentage >= 25) return "E";
        return "F";
    }

    @Override
    public boolean hasPassed(double percentage) {
        return percentage >= 40;
    }

    @Override
    @Transactional
    public void recalculateOnGradeChange(UUID studentId, UUID classId, UUID subjectId,
                                          Integer semester, String academicYear) {
        log.info("Recalculating averages after grade change for student {}", studentId);

        try {
            // Recalculate subject semester average
            calculateSubjectSemesterAverage(studentId, subjectId, semester, academicYear);

            // Recalculate overall semester average
            calculateOverallSemesterAverage(studentId, semester, academicYear);

            // Recalculate rankings
            calculateClassRankings(classId, semester, academicYear);
            calculateSubjectRankings(classId, subjectId, semester, academicYear);

        } catch (Exception e) {
            log.warn("Error recalculating after grade change: {}", e.getMessage());
        }
    }

    private void saveOrUpdateAverage(UUID teacherId, UUID studentId, UUID classId, UUID subjectId,
                                      Integer semester, String academicYear,
                                      AverageType averageType, BigDecimal score) {
        GradeAverage existing;

        if (subjectId != null && semester != null) {
            // Subject semester average
            existing = gradeAverageRepository
                    .findByTeacherIdAndStudentIdAndSubjectIdAndSemesterAndAcademicYearAndAverageType(
                            teacherId, studentId, subjectId, semester, academicYear, averageType)
                    .orElse(null);
        } else if (semester != null) {
            // Overall semester average
            existing = gradeAverageRepository
                    .findByTeacherIdAndStudentIdAndSemesterAndAcademicYearAndAverageType(
                            teacherId, studentId, semester, academicYear, averageType)
                    .orElse(null);
        } else {
            // Annual average
            existing = gradeAverageRepository
                    .findAnnualAverage(teacherId, studentId, academicYear, averageType)
                    .orElse(null);
        }

        GradeAverage average = existing != null ? existing : new GradeAverage();
        average.setTeacherId(teacherId);
        average.setStudentId(studentId);
        average.setClassId(classId);
        if (subjectId != null) {
            Subject subject = subjectRepository.findById(subjectId).orElse(null);
            average.setSubject(subject);
        }
        average.setSemester(semester);
        average.setAcademicYear(academicYear);
        average.setAverageType(averageType);
        average.setAverageScore(score);
        average.setLetterGrade(getLetterGrade(score.doubleValue()));

        gradeAverageRepository.save(average);
    }
}
