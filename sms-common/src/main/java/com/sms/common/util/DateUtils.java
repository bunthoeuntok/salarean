package com.sms.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Date and time utilities for Salarean SMS.
 * Focus on Cambodia-specific business logic.
 *
 * Academic Year in Cambodia: November 1 - August 31
 * Timezone: Asia/Phnom_Penh (UTC+7)
 */
public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ============================================
    // CONSTANTS
    // ============================================

    /** Cambodia timezone (UTC+7) */
    public static final ZoneId CAMBODIA_ZONE = ZoneId.of("Asia/Phnom_Penh");

    /** Khmer locale */
    public static final Locale KHMER_LOCALE = new Locale("km", "KH");

    /** Khmer date format (dd-MM-yyyy) */
    public static final DateTimeFormatter KHMER_DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Display date format (dd MMM yyyy) */
    public static final DateTimeFormatter DISPLAY_DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd MMM yyyy");

    // Academic year boundaries (Cambodia)
    /** Academic year start month (November) */
    public static final int ACADEMIC_YEAR_START_MONTH = 11;

    /** Academic year start day (1st) */
    public static final int ACADEMIC_YEAR_START_DAY = 1;

    /** Academic year end month (August) */
    public static final int ACADEMIC_YEAR_END_MONTH = 8;

    /** Academic year end day (31st) */
    public static final int ACADEMIC_YEAR_END_DAY = 31;


    // ============================================
    // CURRENT TIME METHODS (Cambodia timezone)
    // ============================================

    /**
     * Get current date in Cambodia timezone
     * @return LocalDate in Asia/Phnom_Penh
     */
    public static LocalDate now() {
        return LocalDate.now(CAMBODIA_ZONE);
    }

    /**
     * Get current date-time in Cambodia timezone
     * @return LocalDateTime in Asia/Phnom_Penh
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(CAMBODIA_ZONE);
    }


    // ============================================
    // KHMER FORMATTING (Cambodia-specific)
    // ============================================

    /**
     * Format date for Khmer display (dd-MM-yyyy)
     * Example: 15-01-2025
     *
     * @param date Date to format
     * @return Formatted date string or null if date is null
     */
    public static String formatKhmerDate(LocalDate date) {
        return date != null ? date.format(KHMER_DATE_FORMAT) : null;
    }

    /**
     * Format date for display (dd MMM yyyy)
     * Example: 15 Jan 2025
     *
     * @param date Date to format
     * @return Formatted date string or null if date is null
     */
    public static String formatDisplayDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMAT) : null;
    }


    // ============================================
    // AGE CALCULATION (School enrollment)
    // ============================================

    /**
     * Calculate age in years from date of birth (as of today)
     *
     * @param dateOfBirth Student's date of birth
     * @return Age in years
     * @throws IllegalArgumentException if dateOfBirth is null or in the future
     */
    public static int calculateAge(LocalDate dateOfBirth) {
        return calculateAge(dateOfBirth, now());
    }

    /**
     * Calculate age as of a specific date
     *
     * @param dateOfBirth Student's date of birth
     * @param asOfDate Date to calculate age as of
     * @return Age in years
     * @throws IllegalArgumentException if either date is null or dateOfBirth is after asOfDate
     */
    public static int calculateAge(LocalDate dateOfBirth, LocalDate asOfDate) {
        if (dateOfBirth == null || asOfDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (dateOfBirth.isAfter(asOfDate)) {
            throw new IllegalArgumentException("Date of birth cannot be after asOfDate");
        }
        return Period.between(dateOfBirth, asOfDate).getYears();
    }

    /**
     * Check if student is eligible for enrollment based on age
     *
     * In Cambodia, students must be at least 6 years old to enroll in Grade 1.
     * This method validates age eligibility for any grade/program.
     *
     * @param dateOfBirth Student's date of birth
     * @param enrollmentDate Date of enrollment
     * @param minimumAge Minimum required age (typically 6 for Grade 1)
     * @return true if student meets minimum age requirement
     * @throws IllegalArgumentException if any parameter is null
     */
    public static boolean isAgeEligible(LocalDate dateOfBirth, LocalDate enrollmentDate, int minimumAge) {
        if (dateOfBirth == null || enrollmentDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (minimumAge < 0) {
            throw new IllegalArgumentException("Minimum age cannot be negative");
        }
        return calculateAge(dateOfBirth, enrollmentDate) >= minimumAge;
    }

    /**
     * Validate date of birth for students
     * Checks:
     * - Not null
     * - Not in the future
     * - Within reasonable age range for students (minAge to maxAge)
     *
     * @param dateOfBirth Date of birth to validate
     * @param minAge Minimum age (e.g., 5 for kindergarten)
     * @param maxAge Maximum age (e.g., 25 for high school)
     * @return true if valid
     */
    public static boolean isValidDateOfBirth(LocalDate dateOfBirth, int minAge, int maxAge) {
        if (dateOfBirth == null || dateOfBirth.isAfter(now())) {
            return false;
        }
        int age = calculateAge(dateOfBirth);
        return age >= minAge && age <= maxAge;
    }


    // ============================================
    // ACADEMIC YEAR METHODS (Cambodia-specific)
    // ============================================

    /**
     * Get the current academic year based on today's date
     *
     * Cambodia academic year: November 1 - August 31
     *
     * Examples:
     * - If today is Dec 15, 2024 → returns "2024-2025"
     * - If today is Oct 15, 2024 → returns "2023-2024"
     * - If today is Nov 1, 2024  → returns "2024-2025"
     *
     * @return Academic year string (e.g., "2024-2025")
     */
    public static String getCurrentAcademicYear() {
        return getAcademicYear(now());
    }

    /**
     * Get academic year for a specific date
     *
     * @param date Date to check
     * @return Academic year string (e.g., "2024-2025")
     * @throws IllegalArgumentException if date is null
     */
    public static String getAcademicYear(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        int year = date.getYear();
        int month = date.getMonthValue();

        // If month is Nov or Dec, academic year is current-next year
        if (month >= ACADEMIC_YEAR_START_MONTH) {
            return year + "-" + (year + 1);
        } else {
            // If month is Jan-Aug, academic year is previous-current year
            return (year - 1) + "-" + year;
        }
    }

    /**
     * Get start date of academic year
     *
     * @param academicYear Academic year string (e.g., "2024-2025")
     * @return Start date (November 1)
     * @throws IllegalArgumentException if academicYear format is invalid
     */
    public static LocalDate getAcademicYearStart(String academicYear) {
        validateAcademicYearFormat(academicYear);
        int startYear = Integer.parseInt(academicYear.split("-")[0]);
        return LocalDate.of(startYear, ACADEMIC_YEAR_START_MONTH, ACADEMIC_YEAR_START_DAY);
    }

    /**
     * Get end date of academic year
     *
     * @param academicYear Academic year string (e.g., "2024-2025")
     * @return End date (August 31)
     * @throws IllegalArgumentException if academicYear format is invalid
     */
    public static LocalDate getAcademicYearEnd(String academicYear) {
        validateAcademicYearFormat(academicYear);
        int endYear = Integer.parseInt(academicYear.split("-")[1]);
        return LocalDate.of(endYear, ACADEMIC_YEAR_END_MONTH, ACADEMIC_YEAR_END_DAY);
    }

    /**
     * Check if a date falls within an academic year
     *
     * @param date Date to check
     * @param academicYear Academic year string (e.g., "2024-2025")
     * @return true if date is within the academic year (inclusive)
     * @throws IllegalArgumentException if date is null or academicYear format is invalid
     */
    public static boolean isInAcademicYear(LocalDate date, String academicYear) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        LocalDate start = getAcademicYearStart(academicYear);
        LocalDate end = getAcademicYearEnd(academicYear);
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Get next academic year
     *
     * Example: If current is "2024-2025", returns "2025-2026"
     *
     * @return Next academic year string
     */
    public static String getNextAcademicYear() {
        String current = getCurrentAcademicYear();
        String[] parts = current.split("-");
        int startYear = Integer.parseInt(parts[0]) + 1;
        int endYear = Integer.parseInt(parts[1]) + 1;
        return startYear + "-" + endYear;
    }

    /**
     * Get previous academic year
     *
     * Example: If current is "2024-2025", returns "2023-2024"
     *
     * @return Previous academic year string
     */
    public static String getPreviousAcademicYear() {
        String current = getCurrentAcademicYear();
        String[] parts = current.split("-");
        int startYear = Integer.parseInt(parts[0]) - 1;
        int endYear = Integer.parseInt(parts[1]) - 1;
        return startYear + "-" + endYear;
    }

    /**
     * Validate enrollment date
     * Must be within current or next academic year
     *
     * @param enrollmentDate Date to validate
     * @return true if enrollment date is valid
     */
    public static boolean isValidEnrollmentDate(LocalDate enrollmentDate) {
        if (enrollmentDate == null) {
            return false;
        }

        String currentYear = getCurrentAcademicYear();
        String nextYear = getNextAcademicYear();

        return isInAcademicYear(enrollmentDate, currentYear)
            || isInAcademicYear(enrollmentDate, nextYear);
    }


    // ============================================
    // TOKEN/SESSION EXPIRATION HELPERS
    // ============================================

    /**
     * Calculate expiration datetime from now
     *
     * @param hours Hours until expiration
     * @return Expiration datetime
     */
    public static LocalDateTime expiresInHours(int hours) {
        return nowDateTime().plusHours(hours);
    }

    /**
     * Calculate expiration datetime from now
     *
     * @param days Days until expiration
     * @return Expiration datetime
     */
    public static LocalDateTime expiresInDays(int days) {
        return nowDateTime().plusDays(days);
    }

    /**
     * Check if datetime has expired (is before now)
     *
     * @param expiryTime Expiry datetime to check
     * @return true if expired
     */
    public static boolean isExpired(LocalDateTime expiryTime) {
        return expiryTime != null && expiryTime.isBefore(nowDateTime());
    }

    /**
     * Check if datetime is still valid (is after now)
     *
     * @param expiryTime Expiry datetime to check
     * @return true if still valid
     */
    public static boolean isValid(LocalDateTime expiryTime) {
        return expiryTime != null && expiryTime.isAfter(nowDateTime());
    }


    // ============================================
    // PRIVATE HELPERS
    // ============================================

    /**
     * Validate academic year format (YYYY-YYYY)
     */
    private static void validateAcademicYearFormat(String academicYear) {
        if (academicYear == null || !academicYear.matches("\\d{4}-\\d{4}")) {
            throw new IllegalArgumentException(
                "Invalid academic year format. Expected format: YYYY-YYYY (e.g., 2024-2025)"
            );
        }

        String[] parts = academicYear.split("-");
        int startYear = Integer.parseInt(parts[0]);
        int endYear = Integer.parseInt(parts[1]);

        if (endYear != startYear + 1) {
            throw new IllegalArgumentException(
                "Invalid academic year. End year must be start year + 1 (e.g., 2024-2025)"
            );
        }
    }
}
