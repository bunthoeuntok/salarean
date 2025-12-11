package com.sms.auth.repository;

import com.sms.auth.model.TeacherSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherSchoolRepository extends JpaRepository<TeacherSchool, UUID> {

    Optional<TeacherSchool> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
