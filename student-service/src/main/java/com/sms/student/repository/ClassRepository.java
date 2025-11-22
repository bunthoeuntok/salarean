package com.sms.student.repository;

import com.sms.student.model.SchoolClass;
import com.sms.student.enums.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassRepository extends JpaRepository<SchoolClass, UUID> {

    // Find classes by school
    List<SchoolClass> findBySchoolId(UUID schoolId);

    // Find classes by school and academic year
    @Query("SELECT c FROM SchoolClass c " +
           "WHERE c.schoolId = :schoolId AND c.academicYear = :academicYear " +
           "ORDER BY c.grade, c.section")
    List<SchoolClass> findBySchoolIdAndAcademicYear(
            @Param("schoolId") UUID schoolId,
            @Param("academicYear") String academicYear);

    // Find classes by teacher
    List<SchoolClass> findByTeacherId(UUID teacherId);

    // Find active classes
    List<SchoolClass> findByStatus(ClassStatus status);

    // Find active classes by school
    @Query("SELECT c FROM SchoolClass c " +
           "WHERE c.schoolId = :schoolId AND c.status = 'ACTIVE' " +
           "ORDER BY c.grade, c.section")
    List<SchoolClass> findActiveClassesBySchool(@Param("schoolId") UUID schoolId);

    // Find classes with available capacity
    @Query("SELECT c FROM SchoolClass c " +
           "WHERE c.status = 'ACTIVE' " +
           "AND (c.maxCapacity IS NULL OR c.studentCount < c.maxCapacity) " +
           "ORDER BY c.grade, c.section")
    List<SchoolClass> findClassesWithCapacity();

    // Find classes with available capacity by school
    @Query("SELECT c FROM SchoolClass c " +
           "WHERE c.schoolId = :schoolId AND c.status = 'ACTIVE' " +
           "AND (c.maxCapacity IS NULL OR c.studentCount < c.maxCapacity) " +
           "ORDER BY c.grade, c.section")
    List<SchoolClass> findClassesWithCapacityBySchool(@Param("schoolId") UUID schoolId);

    // Check if class has capacity
    @Query("SELECT CASE WHEN (c.maxCapacity IS NULL OR c.studentCount < c.maxCapacity) " +
           "THEN true ELSE false END " +
           "FROM SchoolClass c WHERE c.id = :classId")
    boolean hasCapacity(@Param("classId") UUID classId);

    // Get current student count
    @Query("SELECT c.studentCount FROM SchoolClass c WHERE c.id = :classId")
    Optional<Integer> getCurrentStudentCount(@Param("classId") UUID classId);

    // Find by grade and section
    @Query("SELECT c FROM SchoolClass c " +
           "WHERE c.schoolId = :schoolId " +
           "AND c.grade = :grade " +
           "AND c.section = :section " +
           "AND c.academicYear = :academicYear")
    Optional<SchoolClass> findBySchoolGradeAndSection(
            @Param("schoolId") UUID schoolId,
            @Param("grade") Integer grade,
            @Param("section") String section,
            @Param("academicYear") String academicYear);

    // Count classes by school
    long countBySchoolId(UUID schoolId);

    // Count active classes by school
    @Query("SELECT COUNT(c) FROM SchoolClass c " +
           "WHERE c.schoolId = :schoolId AND c.status = 'ACTIVE'")
    long countActiveClassesBySchool(@Param("schoolId") UUID schoolId);
}
