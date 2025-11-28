package com.sms.student.service;

import com.sms.student.dto.ParentContactRequest;
import com.sms.student.dto.StudentRequest;
import com.sms.student.dto.StudentResponse;
import com.sms.student.model.ParentContact;
import com.sms.student.model.SchoolClass;
import com.sms.student.model.Student;
import com.sms.student.model.StudentClassEnrollment;
import com.sms.student.enums.DeletionReason;
import com.sms.student.enums.Gender;
import com.sms.student.enums.Relationship;
import com.sms.student.enums.StudentStatus;
import com.sms.student.exception.*;
import com.sms.student.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudentService.createStudent() method.
 * Uses Mockito for mocking repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ParentContactRepository parentContactRepository;

    @Mock
    private StudentClassEnrollmentRepository enrollmentRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private StudentService studentService;

    private StudentRequest validRequest;
    private SchoolClass mockClass;
    private Student mockStudent;

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

        // Create mock class
        mockClass = SchoolClass.builder()
                .id(validRequest.getClassId())
                .grade(5)
                .section("A")
                .maxCapacity(30)
                .studentCount(10)
                .build();

        // Create mock student
        mockStudent = Student.builder()
                .id(UUID.randomUUID())
                .studentCode("STU-2025-1234")
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(2010, 1, 15))
                .gender(Gender.F)
                .status(StudentStatus.ACTIVE)
                .build();
    }

    @Test
    void createStudent_WithValidData_ShouldReturnStudentResponse() {
        // Arrange
        when(classRepository.findById(any())).thenReturn(Optional.of(mockClass));
        when(studentRepository.existsByStudentCode(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);
        when(parentContactRepository.saveAll(anyList())).thenReturn(List.of());
        when(enrollmentRepository.save(any(StudentClassEnrollment.class))).thenReturn(null);
        when(classRepository.save(any(SchoolClass.class))).thenReturn(mockClass);

        // Act
        StudentResponse response = studentService.createStudent(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(mockStudent.getId());
        assertThat(response.getStudentCode()).isEqualTo(mockStudent.getStudentCode());
        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Doe");

        // Verify interactions
        verify(studentRepository, times(1)).save(any(Student.class));
        verify(parentContactRepository, times(1)).saveAll(anyList());
        verify(enrollmentRepository, times(1)).save(any(StudentClassEnrollment.class));
        verify(classRepository, times(1)).save(any(SchoolClass.class));
    }

    @Test
    void createStudent_WithoutParentContacts_ShouldThrowException() {
        // Arrange
        validRequest.setParentContacts(List.of());

        // Act & Assert
        assertThatThrownBy(() -> studentService.createStudent(validRequest))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("At least one parent contact is required");

        // Verify no repository interactions
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_WithNoPrimaryContact_ShouldThrowException() {
        // Arrange
        ParentContactRequest contact = ParentContactRequest.builder()
                .fullName("John Doe")
                .phoneNumber("+85512345678")
                .relationship(Relationship.FATHER)
                .isPrimary(false)
                .build();
        validRequest.setParentContacts(List.of(contact));

        // Act & Assert
        assertThatThrownBy(() -> studentService.createStudent(validRequest))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("primary");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_WithMultiplePrimaryContacts_ShouldThrowException() {
        // Arrange
        ParentContactRequest contact1 = ParentContactRequest.builder()
                .fullName("John Doe")
                .phoneNumber("+85512345678")
                .relationship(Relationship.FATHER)
                .isPrimary(true)
                .build();
        ParentContactRequest contact2 = ParentContactRequest.builder()
                .fullName("Jane Doe")
                .phoneNumber("+85587654321")
                .relationship(Relationship.MOTHER)
                .isPrimary(true)
                .build();
        validRequest.setParentContacts(List.of(contact1, contact2));

        // Act & Assert
        assertThatThrownBy(() -> studentService.createStudent(validRequest))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("Only one parent contact can be marked as primary");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_WithNonExistentClass_ShouldThrowException() {
        // Arrange
        when(classRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.createStudent(validRequest))
                .isInstanceOf(com.sms.student.exception.ClassNotFoundException.class)
                .hasMessageContaining("not found");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_WithFullClass_ShouldThrowException() {
        // Arrange
        mockClass.setStudentCount(30); // At capacity
        when(classRepository.findById(any())).thenReturn(Optional.of(mockClass));

        // Act & Assert
        assertThatThrownBy(() -> studentService.createStudent(validRequest))
                .isInstanceOf(ClassCapacityExceededException.class)
                .hasMessageContaining("full capacity");

        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_WithDuplicateStudentCode_ShouldRetryAndFail() {
        // Arrange
        when(classRepository.findById(any())).thenReturn(Optional.of(mockClass));
        when(studentRepository.existsByStudentCode(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> studentService.createStudent(validRequest))
                .isInstanceOf(DuplicateStudentCodeException.class)
                .hasMessageContaining("Failed to generate unique student code");

        verify(studentRepository, never()).save(any());
        // First call + 10 retries = 11 total calls
        verify(studentRepository, times(11)).existsByStudentCode(anyString());
    }

    @Test
    void createStudent_WithoutClassId_ShouldCreateWithoutEnrollment() {
        // Arrange
        validRequest.setClassId(null);
        when(studentRepository.existsByStudentCode(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);
        when(parentContactRepository.saveAll(anyList())).thenReturn(List.of());

        // Act
        StudentResponse response = studentService.createStudent(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCurrentClassId()).isNull();

        // Verify no enrollment created
        verify(enrollmentRepository, never()).save(any());
        verify(classRepository, never()).save(any());
    }

    @Test
    void createStudent_GeneratesStudentCodeInCorrectFormat() {
        // Arrange
        when(classRepository.findById(any())).thenReturn(Optional.of(mockClass));
        when(studentRepository.existsByStudentCode(anyString())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            // Verify student code format: STU-YYYY-NNNN
            assertThat(student.getStudentCode()).matches("STU-\\d{4}-\\d{4}");
            return mockStudent;
        });
        when(parentContactRepository.saveAll(anyList())).thenReturn(List.of());
        when(enrollmentRepository.save(any())).thenReturn(null);
        when(classRepository.save(any())).thenReturn(mockClass);

        // Act
        studentService.createStudent(validRequest);

        // Assert handled in save mock
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void getStudentById_WithValidId_ShouldReturnStudentResponse() {
        // Arrange
        UUID studentId = mockStudent.getId();
        List<ParentContact> contacts = List.of(
                ParentContact.builder()
                        .id(UUID.randomUUID())
                        .fullName("John Doe")
                        .phoneNumber("+85512345678")
                        .relationship(Relationship.FATHER)
                        .isPrimary(true)
                        .build()
        );

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(contacts);
        when(enrollmentRepository.findCurrentClassIdByStudentId(studentId))
                .thenReturn(Optional.of(UUID.randomUUID()));

        // Act
        StudentResponse response = studentService.getStudentById(studentId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(studentId);
        assertThat(response.getStudentCode()).isEqualTo(mockStudent.getStudentCode());
        assertThat(response.getParentContacts()).hasSize(1);
        assertThat(response.getAge()).isNotNull();

        verify(studentRepository, times(1)).findById(studentId);
        verify(parentContactRepository, times(1)).findByStudentId(studentId);
    }

    @Test
    void getStudentById_WithNonExistentId_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(studentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentById(nonExistentId))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("not found");

        verify(studentRepository, times(1)).findById(nonExistentId);
        verify(parentContactRepository, never()).findByStudentId(any());
    }

    @Test
    void getStudentByCode_WithValidCode_ShouldReturnStudentResponse() {
        // Arrange
        String studentCode = "STU-2025-1234";
        List<ParentContact> contacts = List.of(
                ParentContact.builder()
                        .id(UUID.randomUUID())
                        .fullName("John Doe")
                        .phoneNumber("+85512345678")
                        .relationship(Relationship.FATHER)
                        .isPrimary(true)
                        .build()
        );

        when(studentRepository.findByStudentCode(studentCode)).thenReturn(Optional.of(mockStudent));
        when(parentContactRepository.findByStudentId(any())).thenReturn(contacts);
        when(enrollmentRepository.findCurrentClassIdByStudentId(any()))
                .thenReturn(Optional.of(UUID.randomUUID()));

        // Act
        StudentResponse response = studentService.getStudentByCode(studentCode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStudentCode()).isEqualTo(mockStudent.getStudentCode());
        assertThat(response.getParentContacts()).hasSize(1);

        verify(studentRepository, times(1)).findByStudentCode(studentCode);
    }

    @Test
    void getStudentByCode_WithNonExistentCode_ShouldThrowException() {
        // Arrange
        String nonExistentCode = "STU-9999-9999";
        when(studentRepository.findByStudentCode(nonExistentCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentByCode(nonExistentCode))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("not found");

        verify(studentRepository, times(1)).findByStudentCode(nonExistentCode);
    }

    @Test
    void listStudentsByClass_WithValidClassId_ShouldReturnPaginatedList() {
        // Arrange
        UUID classId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        List<Student> students = List.of(mockStudent);
        Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

        when(classRepository.findById(classId)).thenReturn(Optional.of(mockClass));
        when(studentRepository.findByClassIdAndStatus(classId, pageable)).thenReturn(studentPage);
        when(enrollmentRepository.findCurrentClassIdByStudentId(any())).thenReturn(Optional.of(classId));
        when(parentContactRepository.findPrimaryContactByStudentId(any())).thenReturn(Optional.empty());

        // Act
        com.sms.student.dto.StudentListResponse response = studentService.listStudentsByClass(classId, pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);

        verify(classRepository, times(1)).findById(classId);
        verify(studentRepository, times(1)).findByClassIdAndStatus(classId, pageable);
    }

    @Test
    void listStudentsByClass_WithNonExistentClass_ShouldThrowException() {
        // Arrange
        UUID nonExistentClassId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(classRepository.findById(nonExistentClassId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.listStudentsByClass(nonExistentClassId, pageable))
                .isInstanceOf(com.sms.student.exception.ClassNotFoundException.class)
                .hasMessageContaining("not found");

        verify(classRepository, times(1)).findById(nonExistentClassId);
        verify(studentRepository, never()).findByClassIdAndStatus(any(), any());
    }

    @Test
    void listActiveStudents_ShouldReturnPaginatedList() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);

        List<Student> students = List.of(mockStudent);
        Page<Student> studentPage = new PageImpl<>(students, pageable, 1);

        when(studentRepository.findByStatus(StudentStatus.ACTIVE, pageable)).thenReturn(studentPage);
        when(enrollmentRepository.findCurrentClassIdByStudentId(any())).thenReturn(Optional.empty());
        when(parentContactRepository.findPrimaryContactByStudentId(any())).thenReturn(Optional.empty());

        // Act
        com.sms.student.dto.StudentListResponse response = studentService.listActiveStudents(pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(1);

        verify(studentRepository, times(1)).findByStatus(StudentStatus.ACTIVE, pageable);
    }

    @Test
    void listStudentsByClass_WithEmptyClass_ShouldReturnEmptyList() {
        // Arrange
        UUID classId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        Page<Student> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(classRepository.findById(classId)).thenReturn(Optional.of(mockClass));
        when(studentRepository.findByClassIdAndStatus(classId, pageable)).thenReturn(emptyPage);

        // Act
        com.sms.student.dto.StudentListResponse response = studentService.listStudentsByClass(classId, pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);

        verify(studentRepository, times(1)).findByClassIdAndStatus(classId, pageable);
    }

    @Test
    void updateStudent_WithValidData_ShouldUpdateSuccessfully() {
        // Arrange
        UUID studentId = mockStudent.getId();
        List<ParentContact> existingContacts = List.of(
                ParentContact.builder()
                        .id(UUID.randomUUID())
                        .fullName("Old Contact")
                        .phoneNumber("+85512345678")
                        .relationship(Relationship.FATHER)
                        .isPrimary(true)
                        .build()
        );

        ParentContactRequest newContactReq = ParentContactRequest.builder()
                .fullName("New Contact")
                .phoneNumber("+85587654321")
                .relationship(Relationship.MOTHER)
                .isPrimary(true)
                .build();

        validRequest.setParentContacts(List.of(newContactReq));

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(parentContactRepository.findByStudentId(studentId))
                .thenReturn(existingContacts)
                .thenReturn(List.of()); // After deletion
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);
        when(parentContactRepository.saveAll(anyList())).thenReturn(List.of());
        when(enrollmentRepository.findCurrentClassIdByStudentId(studentId)).thenReturn(Optional.empty());

        // Act
        StudentResponse response = studentService.updateStudent(studentId, validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(studentId);

        verify(studentRepository, times(1)).findById(studentId);
        verify(parentContactRepository, times(1)).deleteAll(existingContacts);
        verify(parentContactRepository, times(1)).saveAll(anyList());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void updateStudent_WithoutParentContacts_ShouldThrowException() {
        // Arrange
        UUID studentId = mockStudent.getId();
        validRequest.setParentContacts(List.of());

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));

        // Act & Assert
        assertThatThrownBy(() -> studentService.updateStudent(studentId, validRequest))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("At least one parent contact is required");

        verify(studentRepository, times(1)).findById(studentId);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateStudent_WithNoPrimaryContact_ShouldThrowException() {
        // Arrange
        UUID studentId = mockStudent.getId();
        ParentContactRequest contact = ParentContactRequest.builder()
                .fullName("John Doe")
                .phoneNumber("+85512345678")
                .relationship(Relationship.FATHER)
                .isPrimary(false)
                .build();
        validRequest.setParentContacts(List.of(contact));

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));

        // Act & Assert
        assertThatThrownBy(() -> studentService.updateStudent(studentId, validRequest))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("primary");

        verify(studentRepository, times(1)).findById(studentId);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateStudent_WithMultiplePrimaryContacts_ShouldThrowException() {
        // Arrange
        UUID studentId = mockStudent.getId();
        ParentContactRequest contact1 = ParentContactRequest.builder()
                .fullName("John Doe")
                .phoneNumber("+85512345678")
                .relationship(Relationship.FATHER)
                .isPrimary(true)
                .build();
        ParentContactRequest contact2 = ParentContactRequest.builder()
                .fullName("Jane Doe")
                .phoneNumber("+85587654321")
                .relationship(Relationship.MOTHER)
                .isPrimary(true)
                .build();
        validRequest.setParentContacts(List.of(contact1, contact2));

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));

        // Act & Assert
        assertThatThrownBy(() -> studentService.updateStudent(studentId, validRequest))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("Only one parent contact can be marked as primary");

        verify(studentRepository, times(1)).findById(studentId);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateStudent_WithNonExistentStudent_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(studentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.updateStudent(nonExistentId, validRequest))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("not found");

        verify(studentRepository, times(1)).findById(nonExistentId);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateStudent_ReplacesAllParentContacts() {
        // Arrange
        UUID studentId = mockStudent.getId();
        List<ParentContact> existingContacts = List.of(
                ParentContact.builder().id(UUID.randomUUID()).fullName("Contact 1").isPrimary(true).build(),
                ParentContact.builder().id(UUID.randomUUID()).fullName("Contact 2").isPrimary(false).build()
        );

        ParentContactRequest newContact = ParentContactRequest.builder()
                .fullName("New Primary Contact")
                .phoneNumber("+85512999999")
                .relationship(Relationship.GUARDIAN)
                .isPrimary(true)
                .build();
        validRequest.setParentContacts(List.of(newContact));

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(existingContacts);
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);
        when(parentContactRepository.saveAll(anyList())).thenReturn(List.of());
        when(enrollmentRepository.findCurrentClassIdByStudentId(studentId)).thenReturn(Optional.empty());

        // Act
        studentService.updateStudent(studentId, validRequest);

        // Assert
        verify(parentContactRepository, times(1)).deleteAll(existingContacts);

        // Capture the saved contacts
        ArgumentCaptor<List<ParentContact>> contactsCaptor = ArgumentCaptor.forClass(List.class);
        verify(parentContactRepository, times(1)).saveAll(contactsCaptor.capture());

        List<ParentContact> savedContacts = contactsCaptor.getValue();
        assertThat(savedContacts).hasSize(1);
        assertThat(savedContacts.get(0).getFullName()).isEqualTo("New Primary Contact");
        assertThat(savedContacts.get(0).getPhoneNumber()).isEqualTo("+85512999999");
        assertThat(savedContacts.get(0).getIsPrimary()).isTrue();
    }

    // ============================================
    // FR-3: Soft Delete Student Tests
    // ============================================

    @Test
    void deleteStudent_WithValidId_ShouldSoftDelete() {
        // Arrange
        UUID studentId = mockStudent.getId();
        UUID deletedBy = UUID.randomUUID();
        DeletionReason reason = DeletionReason.GRADUATED;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);

        // Act
        studentService.deleteStudent(studentId, reason, deletedBy);

        // Assert
        verify(studentRepository, times(1)).findById(studentId);
        verify(studentRepository, times(1)).save(argThat(student ->
                student.getStatus() == StudentStatus.INACTIVE &&
                student.getDeletedBy().equals(deletedBy) &&
                student.getDeletionReason().equals(reason.name()) &&
                student.getDeletedAt() != null
        ));
    }

    @Test
    void deleteStudent_WithNonExistentId_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        when(studentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentService.deleteStudent(nonExistentId, DeletionReason.GRADUATED, deletedBy))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("Student with ID " + nonExistentId + " not found");

        verify(studentRepository, times(1)).findById(nonExistentId);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deleteStudent_WithNullReason_ShouldSoftDeleteWithoutReason() {
        // Arrange
        UUID studentId = mockStudent.getId();
        UUID deletedBy = UUID.randomUUID();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);

        // Act
        studentService.deleteStudent(studentId, null, deletedBy);

        // Assert
        verify(studentRepository, times(1)).save(argThat(student ->
                student.getStatus() == StudentStatus.INACTIVE &&
                student.getDeletedBy().equals(deletedBy) &&
                student.getDeletionReason() == null &&
                student.getDeletedAt() != null
        ));
    }

    @Test
    void deleteStudent_WithTransferredReason_ShouldRecordReason() {
        // Arrange
        UUID studentId = mockStudent.getId();
        UUID deletedBy = UUID.randomUUID();
        DeletionReason reason = DeletionReason.TRANSFERRED;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);

        // Act
        studentService.deleteStudent(studentId, reason, deletedBy);

        // Assert
        verify(studentRepository, times(1)).save(argThat(student ->
                student.getDeletionReason().equals(DeletionReason.TRANSFERRED.name())
        ));
    }

    @Test
    void deleteStudent_WithWithdrewReason_ShouldRecordReason() {
        // Arrange
        UUID studentId = mockStudent.getId();
        UUID deletedBy = UUID.randomUUID();
        DeletionReason reason = DeletionReason.WITHDREW;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(mockStudent);

        // Act
        studentService.deleteStudent(studentId, reason, deletedBy);

        // Assert
        verify(studentRepository, times(1)).save(argThat(student ->
                student.getDeletionReason().equals(DeletionReason.WITHDREW.name())
        ));
    }
}
