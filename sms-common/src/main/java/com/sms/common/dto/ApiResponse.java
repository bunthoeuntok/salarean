package com.sms.common.dto;

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
public class ApiResponse<T> {

    /**
     * Error code for client-side i18n lookup.
     * "SUCCESS" for successful operations.
     * Error codes like "INVALID_INPUT", "NOT_FOUND", etc. for errors.
     *
     * Can be either common ErrorCode or service-specific error codes
     * (e.g., AuthErrorCode, StudentErrorCode, etc.)
     *
     * When serialized to JSON, the enum name is used (e.g., "INVALID_INPUT")
     */
    private Enum<?> errorCode;

    /**
     * Response payload. Null on errors.
     */
    private T data;

    /**
     * No-args constructor
     */
    public ApiResponse() {
    }

    /**
     * All-args constructor
     */
    public ApiResponse(Enum<?> errorCode, T data) {
        this.errorCode = errorCode;
        this.data = data;
    }

    // Getters and Setters
    public Enum<?> getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Enum<?> errorCode) {
        this.errorCode = errorCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * Create successful response with data
     *
     * @param data Response data
     * @param <T> Type of response data
     * @return ApiResponse with SUCCESS error code
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS, data);
    }

    /**
     * Create error response with common error code
     *
     * @param errorCode Common error code from ErrorCode enum
     * @param <T> Type of response data (will be null)
     * @return ApiResponse with error code and null data
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode, null);
    }

    /**
     * Create error response with any enum error code
     * (supports service-specific error codes like AuthErrorCode, StudentErrorCode, etc.)
     *
     * @param errorCode Service-specific error code enum
     * @param <T> Type of response data (will be null)
     * @return ApiResponse with error code and null data
     */
    public static <T> ApiResponse<T> error(Enum<?> errorCode) {
        return new ApiResponse<>(errorCode, null);
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
