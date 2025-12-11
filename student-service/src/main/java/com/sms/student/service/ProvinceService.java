package com.sms.student.service;

import com.sms.student.dto.ProvinceResponse;
import com.sms.student.model.Province;
import com.sms.student.repository.ProvinceRepository;
import com.sms.student.service.interfaces.IProvinceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProvinceService implements IProvinceService {

    private final ProvinceRepository provinceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceResponse> getAllProvinces() {
        log.debug("Fetching all provinces");

        List<Province> provinces = provinceRepository.findAllByOrderByNameAsc();

        log.debug("Found {} provinces", provinces.size());

        return provinces.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProvinceResponse mapToResponse(Province province) {
        return ProvinceResponse.builder()
                .id(province.getId())
                .name(province.getName())
                .nameKhmer(province.getNameKhmer())
                .code(province.getCode())
                .build();
    }
}
