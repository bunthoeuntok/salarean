package com.sms.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sms.common.dto.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private ErrorCode errorCode;
    private T data;

    // Success response
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS, data);
    }

    // Error response
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode, null);
    }
}
