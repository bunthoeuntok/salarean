package com.sms.student.repository;

import com.sms.student.entity.School;
import com.sms.student.enums.SchoolType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchoolRepository extends JpaRepository<School, UUID> {

    // Find schools by type
    List<School> findByType(SchoolType type);

    // Find schools by province
    List<School> findByProvince(String province);

    // Find schools by district
    List<School> findByProvinceAndDistrict(String province, String district);

    // Check if school exists
    boolean existsById(UUID id);
}
