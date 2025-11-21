package com.sms.student.repository;

import com.sms.student.entity.Student;
import com.sms.student.enums.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    // Find by student code
    Optional<Student> findByStudentCode(String studentCode);

    // Check if student code exists
    boolean existsByStudentCode(String studentCode);

    // Find active students
    List<Student> findByStatus(StudentStatus status);

    // Find students by class (via enrollment)
    @Query("SELECT DISTINCT s FROM Student s " +
           "JOIN StudentClassEnrollment sce ON s.id = sce.studentId " +
           "WHERE sce.classId = :classId " +
           "AND sce.endDate IS NULL " +
           "AND s.status = 'ACTIVE'")
    Page<Student> findByClassIdAndStatus(@Param("classId") UUID classId, Pageable pageable);

    // Find all active students with pagination
    @Query("SELECT s FROM Student s WHERE s.status = :status")
    Page<Student> findActiveStudents(@Param("status") StudentStatus status, Pageable pageable);

    // Find students by status with pagination
    Page<Student> findByStatus(StudentStatus status, Pageable pageable);

    // Search students by name (full-text search)
    @Query(value = "SELECT * FROM students s " +
           "WHERE to_tsvector('simple', COALESCE(s.first_name, '') || ' ' || " +
           "COALESCE(s.last_name, '') || ' ' || " +
           "COALESCE(s.first_name_km, '') || ' ' || " +
           "COALESCE(s.last_name_km, '') || ' ' || " +
           "COALESCE(s.student_code, '')) @@ plainto_tsquery('simple', :searchTerm) " +
           "AND s.status = 'ACTIVE'",
           nativeQuery = true)
    Page<Student> searchStudents(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Count students by status
    long countByStatus(StudentStatus status);

    // Find students without class assignment
    @Query("SELECT s FROM Student s WHERE s.id NOT IN " +
           "(SELECT sce.studentId FROM StudentClassEnrollment sce WHERE sce.endDate IS NULL) " +
           "AND s.status = 'ACTIVE'")
    List<Student> findStudentsWithoutClass();
}
