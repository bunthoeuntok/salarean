package com.sms.student.service.interfaces;

import com.sms.student.dto.ProvinceResponse;

import java.util.List;

public interface IProvinceService {

    /**
     * Retrieve all provinces ordered alphabetically by name.
     *
     * @return List of all provinces
     */
    List<ProvinceResponse> getAllProvinces();
}
