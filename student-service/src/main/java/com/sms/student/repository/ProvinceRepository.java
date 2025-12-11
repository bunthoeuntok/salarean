package com.sms.student.repository;

import com.sms.student.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, UUID> {

    Optional<Province> findByCode(String code);

    Optional<Province> findByName(String name);

    List<Province> findAllByOrderByNameAsc();
}
