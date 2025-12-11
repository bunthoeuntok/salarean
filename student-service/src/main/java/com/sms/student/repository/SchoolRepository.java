package com.sms.student.repository;

import com.sms.student.model.School;
import com.sms.student.enums.SchoolType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchoolRepository extends JpaRepository<School, UUID> {

    // Find schools by type
    List<School> findByType(SchoolType type);

    // Find schools by province (deprecated - use findByProvinceId instead)
    @Deprecated
    List<School> findByProvince(String province);

    // Find schools by district (deprecated - use findByDistrictId instead)
    @Deprecated
    List<School> findByProvinceAndDistrict(String province, String district);

    // NEW: Find schools by province ID
    List<School> findByProvinceId(UUID provinceId);

    // NEW: Find schools by district ID
    List<School> findByDistrictId(UUID districtId);

    // NEW: Check if school name exists in district
    boolean existsByDistrictIdAndName(UUID districtId, String name);

    // Check if school exists
    boolean existsById(UUID id);
}
