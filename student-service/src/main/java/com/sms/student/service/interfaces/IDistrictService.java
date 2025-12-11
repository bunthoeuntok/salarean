package com.sms.student.service.interfaces;

import com.sms.student.dto.DistrictResponse;

import java.util.List;
import java.util.UUID;

public interface IDistrictService {

    /**
     * Retrieve all districts for a specific province ordered alphabetically by name.
     *
     * @param provinceId UUID of the province
     * @return List of districts in the province
     */
    List<DistrictResponse> getDistrictsByProvince(UUID provinceId);
}
