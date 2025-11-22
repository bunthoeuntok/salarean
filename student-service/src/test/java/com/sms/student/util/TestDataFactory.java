package com.sms.student.util;

import com.sms.student.dto.ParentContactRequest;
import com.sms.student.dto.StudentRequest;
import com.sms.student.model.ParentContact;
import com.sms.student.model.SchoolClass;
import com.sms.student.model.Student;
import com.sms.student.model.StudentClassEnrollment;
import com.sms.student.enums.EnrollmentReason;
import com.sms.student.enums.Gender;
import com.sms.student.enums.Relationship;
import com.sms.student.enums.StudentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test data factory for creating test entities and DTOs.
 * Provides consistent test data creation for unit and integration tests.
 */
public class TestDataFactory {

    /**
     * Create a test student entity with default values
     */
    public static Student createTestStudent() {
        return createTestStudent("John", "Doe", "STU-2025-0001");
    }

    /**
     * Create a test student entity with custom names and code
     */
    public static Student createTestStudent(String firstName, String lastName, String studentCode) {
        return Student.builder()
                .id(UUID.randomUUID())
                .studentCode(studentCode)
                .firstName(firstName)
                .lastName(lastName)
                .firstNameKhmer("ចន")
                .lastNameKhmer("ដូ")
                .dateOfBirth(LocalDate.of(2010, 1, 15))
                .gender(Gender.M)
                .address("Phnom Penh, Cambodia")
                .emergencyContact("+855999888777")
                .enrollmentDate(LocalDate.now())
                .status(StudentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a test student with parent contacts
     */
    public static Student createTestStudentWithContacts() {
        Student student = createTestStudent();
        List<ParentContact> contacts = new ArrayList<>();
        contacts.add(createTestParentContact(student, true));
        contacts.add(createTestParentContact(student, "John Doe Sr.", "+855987654321", Relationship.FATHER, false));
        student.setParentContacts(contacts);
        return student;
    }

    /**
     * Create a test parent contact entity
     */
    public static ParentContact createTestParentContact(Student student, boolean isPrimary) {
        return createTestParentContact(student, "Jane Doe", "+855123456789", Relationship.MOTHER, isPrimary);
    }

    /**
     * Create a test parent contact entity with custom values
     */
    public static ParentContact createTestParentContact(Student student, String fullName,
                                                       String phoneNumber, Relationship relationship,
                                                       boolean isPrimary) {
        return ParentContact.builder()
                .id(UUID.randomUUID())
                .student(student)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .relationship(relationship)
                .isPrimary(isPrimary)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a test school class entity
     */
    public static SchoolClass createTestClass() {
        return createTestClass(1, "A", 30);
    }

    /**
     * Create a test school class entity with custom values
     */
    public static SchoolClass createTestClass(int grade, String section, int maxCapacity) {
        return SchoolClass.builder()
                .id(UUID.randomUUID())
                .schoolId(UUID.randomUUID())
                .teacherId(UUID.randomUUID())
                .grade(grade)
                .section(section)
                .maxCapacity(maxCapacity)
                .studentCount(0)
                .academicYear("2024-2025")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a test student class enrollment
     */
    public static StudentClassEnrollment createTestEnrollment(Student student, SchoolClass schoolClass) {
        return StudentClassEnrollment.builder()
                .id(UUID.randomUUID())
                .studentId(student.getId())
                .classId(schoolClass.getId())
                .enrollmentDate(LocalDate.now())
                .reason(EnrollmentReason.NEW)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a test StudentRequest DTO
     */
    public static StudentRequest createTestStudentRequest() {
        return createTestStudentRequest("John", "Doe", null);
    }

    /**
     * Create a test StudentRequest DTO with custom values
     */
    public static StudentRequest createTestStudentRequest(String firstName, String lastName, UUID classId) {
        List<ParentContactRequest> parentContacts = new ArrayList<>();
        parentContacts.add(ParentContactRequest.builder()
                .fullName("Jane Doe")
                .phoneNumber("+855123456789")
                .relationship(Relationship.MOTHER)
                .isPrimary(true)
                .build());

        return StudentRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .firstNameKhmer("ចន")
                .lastNameKhmer("ដូ")
                .dateOfBirth(LocalDate.of(2010, 1, 15))
                .gender(Gender.M)
                .address("Phnom Penh, Cambodia")
                .emergencyContact("+855999888777")
                .enrollmentDate(LocalDate.now())
                .classId(classId)
                .parentContacts(parentContacts)
                .build();
    }

    /**
     * Create a ParentContactRequest DTO
     */
    public static ParentContactRequest createTestParentContactRequest(boolean isPrimary) {
        return ParentContactRequest.builder()
                .fullName("Jane Doe")
                .phoneNumber("+855123456789")
                .relationship(Relationship.MOTHER)
                .isPrimary(isPrimary)
                .build();
    }

    /**
     * Create multiple test students for bulk operations
     */
    public static List<Student> createTestStudents(int count) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            students.add(createTestStudent(
                    "Student" + i,
                    "Test" + i,
                    String.format("STU-2025-%04d", i + 1)
            ));
        }
        return students;
    }

    /**
     * Create multiple test StudentRequests for bulk operations
     */
    public static List<StudentRequest> createTestStudentRequests(int count) {
        List<StudentRequest> requests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            requests.add(createTestStudentRequest(
                    "Student" + i,
                    "Test" + i,
                    null
            ));
        }
        return requests;
    }
}
