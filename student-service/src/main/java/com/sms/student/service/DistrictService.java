package com.sms.student.service;

import com.sms.student.dto.DistrictResponse;
import com.sms.student.exception.ProvinceNotFoundException;
import com.sms.student.model.District;
import com.sms.student.repository.DistrictRepository;
import com.sms.student.repository.ProvinceRepository;
import com.sms.student.service.interfaces.IDistrictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistrictService implements IDistrictService {

    private final DistrictRepository districtRepository;
    private final ProvinceRepository provinceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DistrictResponse> getDistrictsByProvince(UUID provinceId) {
        log.debug("Fetching districts for province ID: {}", provinceId);

        // Validate province exists
        if (!provinceRepository.existsById(provinceId)) {
            log.warn("Province not found with ID: {}", provinceId);
            throw new ProvinceNotFoundException("Province not found with ID: " + provinceId);
        }

        List<District> districts = districtRepository.findByProvinceIdOrderByNameAsc(provinceId);

        log.debug("Found {} districts for province ID: {}", districts.size(), provinceId);

        return districts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DistrictResponse mapToResponse(District district) {
        return DistrictResponse.builder()
                .id(district.getId())
                .provinceId(district.getProvinceId())
                .name(district.getName())
                .nameKhmer(district.getNameKhmer())
                .code(district.getCode())
                .build();
    }
}
