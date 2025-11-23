package com.sms.common.constants;

/**
 * Common constants shared across all Salarean SMS microservices.
 *
 * Single source of truth for:
 * - File size limits and MIME types
 * - Pagination defaults
 * - Date/time formats (Cambodia timezone and date formats)
 * - Academic year settings (Cambodia)
 * - Age validation rules (school enrollment)
 * - HTTP headers and language codes
 *
 * NOTE: Service-specific constants (JWT, passwords, rate limiting, etc.)
 * should be defined in their respective services, NOT here.
 */
public final class CommonConstants {

    private CommonConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ============================================
    // FILE UPLOAD LIMITS
    // ============================================

    /** Maximum photo file size: 5MB */
    public static final long MAX_PHOTO_SIZE_BYTES = 5 * 1024 * 1024;

    /** Maximum document file size: 10MB */
    public static final long MAX_DOCUMENT_SIZE_BYTES = 10 * 1024 * 1024;

    /** Maximum video file size: 100MB */
    public static final long MAX_VIDEO_SIZE_BYTES = 100 * 1024 * 1024;


    // ============================================
    // PAGINATION
    // ============================================

    /** Default page size for paginated results */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** Maximum page size allowed */
    public static final int MAX_PAGE_SIZE = 100;

    /** Minimum page size allowed */
    public static final int MIN_PAGE_SIZE = 1;


    // ============================================
    // DATE/TIME FORMATS
    // ============================================

    /** ISO date format: yyyy-MM-dd */
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd";

    /** Display date format: dd MMM yyyy (e.g., 15 Jan 2025) */
    public static final String DATE_FORMAT_DISPLAY = "dd MMM yyyy";

    /** Khmer date format: dd-MM-yyyy */
    public static final String DATE_FORMAT_KHMER = "dd-MM-yyyy";

    /** Full datetime format: yyyy-MM-dd HH:mm:ss */
    public static final String DATETIME_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";

    /** Cambodia timezone: Asia/Phnom_Penh (UTC+7) */
    public static final String TIMEZONE_CAMBODIA = "Asia/Phnom_Penh";


    // ============================================
    // VALIDATION - AGE LIMITS (School enrollment)
    // ============================================

    /** Minimum age for kindergarten enrollment */
    public static final int MIN_KINDERGARTEN_AGE = 3;

    /** Minimum age for Grade 1 enrollment (Cambodia standard) */
    public static final int MIN_GRADE_1_AGE = 6;

    /** Maximum age for high school student */
    public static final int MAX_HIGH_SCHOOL_AGE = 25;


    // ============================================
    // ACADEMIC YEAR (Cambodia)
    // ============================================

    /** Academic year start month (November) */
    public static final int ACADEMIC_YEAR_START_MONTH = 11;

    /** Academic year start day (1st) */
    public static final int ACADEMIC_YEAR_START_DAY = 1;

    /** Academic year end month (August) */
    public static final int ACADEMIC_YEAR_END_MONTH = 8;

    /** Academic year end day (31st) */
    public static final int ACADEMIC_YEAR_END_DAY = 31;


    // ============================================
    // HTTP HEADERS
    // ============================================

    /** Authorization header name */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /** Bearer token prefix */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Content-Type header name */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /** Accept-Language header name */
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";


    // ============================================
    // SUPPORTED LANGUAGES
    // ============================================

    /** English language code */
    public static final String LANG_ENGLISH = "en";

    /** Khmer language code */
    public static final String LANG_KHMER = "km";

    /** Default language */
    public static final String DEFAULT_LANGUAGE = LANG_ENGLISH;


    // ============================================
    // FILE MIME TYPES
    // ============================================

    /** Allowed image MIME types */
    public static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg",
        "image/png",
        "image/webp"
    };

    /** Allowed document MIME types */
    public static final String[] ALLOWED_DOCUMENT_TYPES = {
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    };
}
