package com.sms.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.student.dto.*;
import com.sms.student.enums.Gender;
import com.sms.student.enums.Relationship;
import com.sms.student.enums.StudentStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sms.student.exception.InvalidStudentDataException;
import com.sms.student.exception.StudentNotFoundException;
import com.sms.student.service.interfaces.IStudentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StudentController.
 * Uses MockMvc for testing REST API endpoints.
 */
@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IStudentService studentService;

    private StudentRequest validRequest;
    private StudentResponse mockResponse;

    @BeforeEach
    void setUp() {
        // Create valid parent contact
        ParentContactRequest parentContact = ParentContactRequest.builder()
                .fullName("John Doe")
                .phoneNumber("+85512345678")
                .relationship(Relationship.FATHER)
                .isPrimary(true)
                .build();

        // Create valid student request
        validRequest = StudentRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .firstNameKhmer("ជេន")
                .lastNameKhmer("ដូ")
                .dateOfBirth(LocalDate.of(2010, 1, 15))
                .gender(Gender.F)
                .address("Phnom Penh, Cambodia")
                .emergencyContact("+85512345678")
                .enrollmentDate(LocalDate.now())
                .classId(UUID.randomUUID())
                .parentContacts(List.of(parentContact))
                .build();

        // Create mock parent contact response
        ParentContactResponse parentContactResponse = ParentContactResponse.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .phoneNumber("+85512345678")
                .relationship(Relationship.FATHER)
                .isPrimary(true)
                .build();

        // Create mock student response
        mockResponse = StudentResponse.builder()
                .id(UUID.randomUUID())
                .studentCode("STU-2025-1234")
                .firstName("Jane")
                .lastName("Doe")
                .firstNameKhmer("ជេន")
                .lastNameKhmer("ដូ")
                .dateOfBirth(LocalDate.of(2010, 1, 15))
                .age(15)
                .gender(Gender.F)
                .address("Phnom Penh, Cambodia")
                .emergencyContact("+85512345678")
                .enrollmentDate(LocalDate.now())
                .status(StudentStatus.ACTIVE)
                .currentClassId(validRequest.getClassId())
                .parentContacts(List.of(parentContactResponse))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createStudent_WithValidData_ShouldReturn201Created() throws Exception {
        // Arrange
        when(studentService.createStudent(any(StudentRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(mockResponse.getId().toString()))
                .andExpect(jsonPath("$.data.studentCode").value("STU-2025-1234"))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.gender").value("F"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.parentContacts").isArray())
                .andExpect(jsonPath("$.data.parentContacts[0].fullName").value("John Doe"));
    }

    @Test
    void createStudent_WithMissingFirstName_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validRequest.setFirstName(null);

        // Act & Assert
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.firstName").exists());
    }

    @Test
    void createStudent_WithInvalidPhoneFormat_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        ParentContactRequest invalidContact = ParentContactRequest.builder()
                .fullName("John Doe")
                .phoneNumber("123456789") // Invalid format
                .relationship(Relationship.FATHER)
                .isPrimary(true)
                .build();
        validRequest.setParentContacts(List.of(invalidContact));

        // Act & Assert
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void createStudent_WithFutureDateOfBirth_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validRequest.setDateOfBirth(LocalDate.now().plusDays(1));

        // Act & Assert
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.dateOfBirth").exists());
    }

    @Test
    void createStudent_WithEmptyParentContacts_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        validRequest.setParentContacts(List.of());

        // Act & Assert
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.parentContacts").exists());
    }

    @Test
    void getStudentById_WithValidId_ShouldReturn200Ok() throws Exception {
        // Arrange
        UUID studentId = mockResponse.getId();
        when(studentService.getStudentById(studentId)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/students/{id}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(studentId.toString()))
                .andExpect(jsonPath("$.data.studentCode").value("STU-2025-1234"));
    }

    @Test
    void getStudentByCode_WithValidCode_ShouldReturn200Ok() throws Exception {
        // Arrange
        String studentCode = "STU-2025-1234";
        when(studentService.getStudentByCode(studentCode)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/students/code/{studentCode}", studentCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.studentCode").value(studentCode));
    }

    @Test
    void listActiveStudents_ShouldReturn200Ok() throws Exception {
        // Arrange
        StudentSummary summary = StudentSummary.builder()
                .id(mockResponse.getId())
                .studentCode("STU-2025-1234")
                .firstName("Jane")
                .lastName("Doe")
                .age(15)
                .currentClassId(validRequest.getClassId())
                .primaryParentContact("John Doe (+85512345678)")
                .build();

        StudentListResponse listResponse = StudentListResponse.builder()
                .content(List.of(summary))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(studentService.listActiveStudents(any())).thenReturn(listResponse);

        // Act & Assert
        mockMvc.perform(get("/api/students")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "lastName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].studentCode").value("STU-2025-1234"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getStudentById_WithNonExistentId_ShouldReturn404NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(studentService.getStudentById(nonExistentId))
                .thenThrow(new com.sms.student.exception.StudentNotFoundException(
                        "Student with ID " + nonExistentId + " not found"));

        // Act & Assert
        mockMvc.perform(get("/api/students/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STUDENT_NOT_FOUND"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getStudentByCode_WithNonExistentCode_ShouldReturn404NotFound() throws Exception {
        // Arrange
        String nonExistentCode = "STU-9999-9999";
        when(studentService.getStudentByCode(nonExistentCode))
                .thenThrow(new com.sms.student.exception.StudentNotFoundException(
                        "Student with code " + nonExistentCode + " not found"));

        // Act & Assert
        mockMvc.perform(get("/api/students/code/{studentCode}", nonExistentCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STUDENT_NOT_FOUND"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void updateStudent_WithValidData_ShouldReturn200Ok() throws Exception {
        // Arrange
        UUID studentId = mockResponse.getId();
        when(studentService.updateStudent(any(UUID.class), any(StudentRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(put("/api/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(studentId.toString()))
                .andExpect(jsonPath("$.data.studentCode").value("STU-2025-1234"));
    }

    @Test
    void deleteStudent_WithValidId_ShouldReturn200Ok() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/students/{id}", studentId)
                        .param("deletedBy", deletedBy.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void deleteStudent_WithGraduatedReason_ShouldReturn200Ok() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/students/{id}", studentId)
                        .param("reason", "GRADUATED")
                        .param("deletedBy", deletedBy.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void deleteStudent_WithTransferredReason_ShouldReturn200Ok() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/students/{id}", studentId)
                        .param("reason", "TRANSFERRED")
                        .param("deletedBy", deletedBy.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"));
    }

    @Test
    void deleteStudent_WithNonExistentId_ShouldReturn404NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        doThrow(new StudentNotFoundException("Student with ID " + nonExistentId + " not found"))
                .when(studentService).deleteStudent(any(UUID.class), any(), any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/students/{id}", nonExistentId)
                        .param("deletedBy", deletedBy.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STUDENT_NOT_FOUND"));
    }

    @Test
    void deleteStudent_WithoutDeletedBy_ShouldReturn200Ok() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();

        // Act & Assert - deletedBy is optional
        mockMvc.perform(delete("/api/students/{id}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"));
    }

    @Test
    void listStudentsByClass_WithValidClassId_ShouldReturn200Ok() throws Exception {
        // Arrange
        UUID classId = UUID.randomUUID();
        StudentSummary summary = StudentSummary.builder()
                .id(mockResponse.getId())
                .studentCode("STU-2025-1234")
                .firstName("Jane")
                .lastName("Doe")
                .age(15)
                .currentClassId(classId)
                .primaryParentContact("John Doe (+85512345678)")
                .build();

        StudentListResponse listResponse = StudentListResponse.builder()
                .content(List.of(summary))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(studentService.listStudentsByClass(any(), any())).thenReturn(listResponse);

        // Act & Assert
        mockMvc.perform(get("/api/students/class/{classId}", classId)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "lastName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].studentCode").value("STU-2025-1234"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void listStudentsByClass_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Arrange
        UUID classId = UUID.randomUUID();
        StudentListResponse listResponse = StudentListResponse.builder()
                .content(List.of())
                .page(2)
                .size(10)
                .totalElements(25)
                .totalPages(3)
                .build();

        when(studentService.listStudentsByClass(any(), any())).thenReturn(listResponse);

        // Act & Assert
        mockMvc.perform(get("/api/students/class/{classId}", classId)
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(3));
    }

    @Test
    void listActiveStudents_WithPaginationAndSorting_ShouldReturn200Ok() throws Exception {
        // Arrange
        StudentSummary summary1 = StudentSummary.builder()
                .id(UUID.randomUUID())
                .studentCode("STU-2025-0001")
                .firstName("Alice")
                .lastName("Anderson")
                .age(14)
                .build();

        StudentSummary summary2 = StudentSummary.builder()
                .id(UUID.randomUUID())
                .studentCode("STU-2025-0002")
                .firstName("Bob")
                .lastName("Brown")
                .age(15)
                .build();

        StudentListResponse listResponse = StudentListResponse.builder()
                .content(List.of(summary1, summary2))
                .page(0)
                .size(20)
                .totalElements(2)
                .totalPages(1)
                .build();

        when(studentService.listActiveStudents(any())).thenReturn(listResponse);

        // Act & Assert
        mockMvc.perform(get("/api/students")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "lastName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").value(org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].lastName").value("Anderson"))
                .andExpect(jsonPath("$.data.content[1].lastName").value("Brown"));
    }

    @Test
    void listActiveStudents_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        // Arrange
        StudentListResponse emptyResponse = StudentListResponse.builder()
                .content(List.of())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(studentService.listActiveStudents(any())).thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void updateStudent_WithNoParentContacts_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();
        validRequest.setParentContacts(List.of());

        // Act & Assert - validation catches this before service layer
        mockMvc.perform(put("/api/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void updateStudent_WithNoPrimaryContact_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();
        ParentContactRequest contact = ParentContactRequest.builder()
                .fullName("John Doe")
                .phoneNumber("+85512345678")
                .relationship(Relationship.FATHER)
                .isPrimary(false)
                .build();
        validRequest.setParentContacts(List.of(contact));

        when(studentService.updateStudent(eq(studentId), any(StudentRequest.class)))
                .thenThrow(new InvalidStudentDataException("Only one parent contact can be marked as primary"));

        // Act & Assert
        mockMvc.perform(put("/api/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STUDENT_DATA"));
    }

    @Test
    void updateStudent_WithMultiplePrimaryContacts_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();
        ParentContactRequest contact1 = ParentContactRequest.builder()
                .fullName("John Doe")
                .phoneNumber("+85512345678")
                .relationship(Relationship.FATHER)
                .isPrimary(true)
                .build();
        ParentContactRequest contact2 = ParentContactRequest.builder()
                .fullName("Jane Smith")
                .phoneNumber("+85512999999")
                .relationship(Relationship.MOTHER)
                .isPrimary(true)
                .build();
        validRequest.setParentContacts(List.of(contact1, contact2));

        when(studentService.updateStudent(eq(studentId), any(StudentRequest.class)))
                .thenThrow(new InvalidStudentDataException("Only one parent contact can be marked as primary"));

        // Act & Assert
        mockMvc.perform(put("/api/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STUDENT_DATA"));
    }

    @Test
    void updateStudent_WithNonExistentStudent_ShouldReturn404NotFound() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();
        when(studentService.updateStudent(eq(studentId), any(StudentRequest.class)))
                .thenThrow(new StudentNotFoundException("Student with ID " + studentId + " not found"));

        // Act & Assert
        mockMvc.perform(put("/api/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STUDENT_NOT_FOUND"));
    }

    @Test
    void updateStudent_WithOptimisticLockingFailure_ShouldReturn400BadRequest() throws Exception {
        // Arrange
        UUID studentId = UUID.randomUUID();
        when(studentService.updateStudent(eq(studentId), any(StudentRequest.class)))
                .thenThrow(new InvalidStudentDataException("Update conflict: Another user has modified this student. Please refresh and try again."));

        // Act & Assert
        mockMvc.perform(put("/api/students/{id}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STUDENT_DATA"));
    }
}
