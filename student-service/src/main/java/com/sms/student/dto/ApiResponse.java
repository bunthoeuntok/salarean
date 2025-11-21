package com.sms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private ErrorCode errorCode;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .errorCode(ErrorCode.SUCCESS)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .errorCode(errorCode)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return ApiResponse.<T>builder()
                .errorCode(errorCode)
                .data(data)
                .build();
    }
}
