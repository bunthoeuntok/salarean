package com.sms.student.repository;

import com.sms.student.model.StudentClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentClassEnrollmentRepository extends JpaRepository<StudentClassEnrollment, UUID> {

    // Find current enrollment for a student
    @Query("SELECT sce FROM StudentClassEnrollment sce " +
           "WHERE sce.studentId = :studentId AND sce.endDate IS NULL")
    Optional<StudentClassEnrollment> findCurrentEnrollmentByStudentId(@Param("studentId") UUID studentId);

    // Find all enrollments for a student (history)
    @Query("SELECT sce FROM StudentClassEnrollment sce " +
           "WHERE sce.studentId = :studentId " +
           "ORDER BY sce.enrollmentDate DESC")
    List<StudentClassEnrollment> findEnrollmentHistoryByStudentId(@Param("studentId") UUID studentId);

    // Find all current enrollments for a class
    @Query("SELECT sce FROM StudentClassEnrollment sce " +
           "WHERE sce.classId = :classId AND sce.endDate IS NULL")
    List<StudentClassEnrollment> findCurrentEnrollmentsByClassId(@Param("classId") UUID classId);

    // Count current enrollments in a class
    @Query("SELECT COUNT(sce) FROM StudentClassEnrollment sce " +
           "WHERE sce.classId = :classId AND sce.endDate IS NULL")
    long countCurrentEnrollmentsByClassId(@Param("classId") UUID classId);

    // Check if student is currently enrolled
    @Query("SELECT COUNT(sce) > 0 FROM StudentClassEnrollment sce " +
           "WHERE sce.studentId = :studentId AND sce.endDate IS NULL")
    boolean isStudentCurrentlyEnrolled(@Param("studentId") UUID studentId);

    // Check if student is enrolled in specific class
    @Query("SELECT COUNT(sce) > 0 FROM StudentClassEnrollment sce " +
           "WHERE sce.studentId = :studentId AND sce.classId = :classId AND sce.endDate IS NULL")
    boolean isStudentEnrolledInClass(@Param("studentId") UUID studentId, @Param("classId") UUID classId);

    // Get student's current class ID
    @Query("SELECT sce.classId FROM StudentClassEnrollment sce " +
           "WHERE sce.studentId = :studentId AND sce.endDate IS NULL")
    Optional<UUID> findCurrentClassIdByStudentId(@Param("studentId") UUID studentId);
}
