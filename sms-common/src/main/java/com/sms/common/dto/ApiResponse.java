package com.sms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response wrapper for all Salarean SMS services.
 *
 * Provides consistent response format across all microservices:
 * - errorCode: Machine-readable error code for client-side i18n lookup
 * - data: Response payload (type-safe with generics)
 *
 * Examples:
 * Success: { "errorCode": "SUCCESS", "data": { "id": 123, "name": "John" } }
 * Error:   { "errorCode": "INVALID_INPUT", "data": null }
 *
 * @param <T> Type of response data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * Error code for client-side i18n lookup.
     * "SUCCESS" for successful operations.
     * Error codes like "INVALID_INPUT", "NOT_FOUND", etc. for errors.
     */
    private ErrorCode errorCode;

    /**
     * Response payload. Null on errors.
     */
    private T data;

    /**
     * Create successful response with data
     *
     * @param data Response data
     * @param <T> Type of response data
     * @return ApiResponse with SUCCESS error code
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(ErrorCode.SUCCESS, data);
    }

    /**
     * Create error response with error code
     *
     * @param errorCode Error code
     * @param <T> Type of response data (will be null)
     * @return ApiResponse with error code and null data
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<T>(errorCode, null);
    }

    /**
     * Check if response is successful
     *
     * @return true if errorCode is SUCCESS
     */
    public boolean isSuccess() {
        return errorCode == ErrorCode.SUCCESS;
    }

    /**
     * Check if response is an error
     *
     * @return true if errorCode is not SUCCESS
     */
    public boolean isError() {
        return errorCode != ErrorCode.SUCCESS;
    }
}
