package com.sms.student.repository;

import com.sms.student.model.ParentContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentContactRepository extends JpaRepository<ParentContact, UUID> {

    // Find all contacts for a student
    @Query("SELECT pc FROM ParentContact pc WHERE pc.student.id = :studentId")
    List<ParentContact> findByStudentId(@Param("studentId") UUID studentId);

    // Find primary contact for a student
    @Query("SELECT pc FROM ParentContact pc WHERE pc.student.id = :studentId AND pc.isPrimary = true")
    Optional<ParentContact> findPrimaryContactByStudentId(@Param("studentId") UUID studentId);

    // Check if student has primary contact
    @Query("SELECT COUNT(pc) > 0 FROM ParentContact pc WHERE pc.student.id = :studentId AND pc.isPrimary = true")
    boolean existsPrimaryContactForStudent(@Param("studentId") UUID studentId);

    // Count contacts for a student
    @Query("SELECT COUNT(pc) FROM ParentContact pc WHERE pc.student.id = :studentId")
    long countByStudentId(@Param("studentId") UUID studentId);

    // Delete all contacts for a student (used during cascade delete)
    void deleteByStudentId(UUID studentId);
}
