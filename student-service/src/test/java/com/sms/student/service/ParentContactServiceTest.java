package com.sms.student.service;

import com.sms.student.dto.ParentContactRequest;
import com.sms.student.dto.ParentContactResponse;
import com.sms.student.model.ParentContact;
import com.sms.student.model.Student;
import com.sms.student.enums.Relationship;
import com.sms.student.exception.InvalidStudentDataException;
import com.sms.student.exception.ParentContactNotFoundException;
import com.sms.student.exception.StudentNotFoundException;
import com.sms.student.repository.ParentContactRepository;
import com.sms.student.repository.StudentRepository;
import com.sms.student.service.ParentContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ParentContactService implementation.
 * Tests CRUD operations and validation logic.
 */
@ExtendWith(MockitoExtension.class)
class ParentContactServiceTest {

    @Mock
    private ParentContactRepository parentContactRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ParentContactService parentContactService;

    private UUID studentId;
    private UUID contactId;
    private Student student;
    private ParentContact parentContact;
    private ParentContactRequest request;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        contactId = UUID.randomUUID();

        student = Student.builder()
                .id(studentId)
                .studentCode("STU-2025-0001")
                .firstName("John")
                .lastName("Doe")
                .build();

        parentContact = ParentContact.builder()
                .id(contactId)
                .student(student)
                .fullName("Jane Doe")
                .phoneNumber("+855123456789")
                .relationship(Relationship.MOTHER)
                .isPrimary(false)
                .build();

        request = ParentContactRequest.builder()
                .fullName("Jane Doe")
                .phoneNumber("+855123456789")
                .relationship(Relationship.MOTHER)
                .isPrimary(false)
                .build();
    }

    // ========== Add Parent Contact Tests ==========

    @Test
    @DisplayName("Add parent contact - Success")
    void addParentContact_Success() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(parentContactRepository.save(any(ParentContact.class))).thenReturn(parentContact);

        // Act
        ParentContactResponse response = parentContactService.addParentContact(studentId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Jane Doe");
        assertThat(response.getPhoneNumber()).isEqualTo("+855123456789");
        assertThat(response.getRelationship()).isEqualTo(Relationship.MOTHER);
        assertThat(response.getIsPrimary()).isFalse();

        verify(studentRepository).findById(studentId);
        // findByStudentId should NOT be called when isPrimary is false
        verify(parentContactRepository, never()).findByStudentId(any());
        verify(parentContactRepository).save(any(ParentContact.class));
    }

    @Test
    @DisplayName("Add parent contact - Student not found")
    void addParentContact_StudentNotFound() {
        // Arrange
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> parentContactService.addParentContact(studentId, request))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("Student with ID " + studentId + " not found");

        verify(studentRepository).findById(studentId);
        verify(parentContactRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add primary contact - Success when no existing primary")
    void addPrimaryContact_Success() {
        // Arrange
        request.setIsPrimary(true);
        ParentContact primaryContact = ParentContact.builder()
                .id(contactId)
                .student(student)
                .fullName("Jane Doe")
                .phoneNumber("+855123456789")
                .relationship(Relationship.MOTHER)
                .isPrimary(true)
                .build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(Collections.emptyList());
        when(parentContactRepository.save(any(ParentContact.class))).thenReturn(primaryContact);

        // Act
        ParentContactResponse response = parentContactService.addParentContact(studentId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsPrimary()).isTrue();

        verify(parentContactRepository).findByStudentId(studentId);
        verify(parentContactRepository).save(any(ParentContact.class));
    }

    @Test
    @DisplayName("Add primary contact - Fails when primary already exists")
    void addPrimaryContact_FailsWhenPrimaryExists() {
        // Arrange
        request.setIsPrimary(true);

        ParentContact existingPrimary = ParentContact.builder()
                .id(UUID.randomUUID())
                .student(student)
                .fullName("John Doe Sr.")
                .phoneNumber("+855987654321")
                .relationship(Relationship.FATHER)
                .isPrimary(true)
                .build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(Collections.singletonList(existingPrimary));

        // Act & Assert
        assertThatThrownBy(() -> parentContactService.addParentContact(studentId, request))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("Only one parent contact can be marked as primary");

        verify(parentContactRepository).findByStudentId(studentId);
        verify(parentContactRepository, never()).save(any());
    }

    // ========== Update Parent Contact Tests ==========

    @Test
    @DisplayName("Update parent contact - Success")
    void updateParentContact_Success() {
        // Arrange
        ParentContactRequest updateRequest = ParentContactRequest.builder()
                .fullName("Jane Smith")
                .phoneNumber("+855111222333")
                .relationship(Relationship.GUARDIAN)
                .isPrimary(false)
                .build();

        ParentContact updatedContact = ParentContact.builder()
                .id(contactId)
                .student(student)
                .fullName("Jane Smith")
                .phoneNumber("+855111222333")
                .relationship(Relationship.GUARDIAN)
                .isPrimary(false)
                .build();

        when(parentContactRepository.findById(contactId)).thenReturn(Optional.of(parentContact));
        when(parentContactRepository.save(any(ParentContact.class))).thenReturn(updatedContact);

        // Act
        ParentContactResponse response = parentContactService.updateParentContact(contactId, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Jane Smith");
        assertThat(response.getPhoneNumber()).isEqualTo("+855111222333");
        assertThat(response.getRelationship()).isEqualTo(Relationship.GUARDIAN);

        verify(parentContactRepository).findById(contactId);
        verify(parentContactRepository).save(any(ParentContact.class));
    }

    @Test
    @DisplayName("Update parent contact - Contact not found")
    void updateParentContact_NotFound() {
        // Arrange
        when(parentContactRepository.findById(contactId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> parentContactService.updateParentContact(contactId, request))
                .isInstanceOf(ParentContactNotFoundException.class)
                .hasMessageContaining("Parent contact with ID " + contactId + " not found");

        verify(parentContactRepository).findById(contactId);
        verify(parentContactRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update to primary - Success when no existing primary")
    void updateToPrimary_Success() {
        // Arrange
        request.setIsPrimary(true);
        parentContact.setIsPrimary(false); // Currently not primary

        ParentContact updatedContact = ParentContact.builder()
                .id(contactId)
                .student(student)
                .fullName("Jane Doe")
                .phoneNumber("+855123456789")
                .relationship(Relationship.MOTHER)
                .isPrimary(true)
                .build();

        when(parentContactRepository.findById(contactId)).thenReturn(Optional.of(parentContact));
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(Collections.singletonList(parentContact));
        when(parentContactRepository.save(any(ParentContact.class))).thenReturn(updatedContact);

        // Act
        ParentContactResponse response = parentContactService.updateParentContact(contactId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsPrimary()).isTrue();

        verify(parentContactRepository).findByStudentId(studentId);
        verify(parentContactRepository).save(any(ParentContact.class));
    }

    @Test
    @DisplayName("Update to primary - Fails when another primary exists")
    void updateToPrimary_FailsWhenAnotherPrimaryExists() {
        // Arrange
        request.setIsPrimary(true);
        parentContact.setIsPrimary(false); // Currently not primary

        ParentContact existingPrimary = ParentContact.builder()
                .id(UUID.randomUUID())
                .student(student)
                .fullName("John Doe Sr.")
                .phoneNumber("+855987654321")
                .relationship(Relationship.FATHER)
                .isPrimary(true)
                .build();

        when(parentContactRepository.findById(contactId)).thenReturn(Optional.of(parentContact));
        when(parentContactRepository.findByStudentId(studentId))
                .thenReturn(Arrays.asList(parentContact, existingPrimary));

        // Act & Assert
        assertThatThrownBy(() -> parentContactService.updateParentContact(contactId, request))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("Only one parent contact can be marked as primary");

        verify(parentContactRepository).findByStudentId(studentId);
        verify(parentContactRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update primary to non-primary - Success")
    void updatePrimaryToNonPrimary_Success() {
        // Arrange
        parentContact.setIsPrimary(true); // Currently primary
        request.setIsPrimary(false); // Changing to non-primary

        ParentContact updatedContact = ParentContact.builder()
                .id(contactId)
                .student(student)
                .fullName("Jane Doe")
                .phoneNumber("+855123456789")
                .relationship(Relationship.MOTHER)
                .isPrimary(false)
                .build();

        when(parentContactRepository.findById(contactId)).thenReturn(Optional.of(parentContact));
        when(parentContactRepository.save(any(ParentContact.class))).thenReturn(updatedContact);

        // Act
        ParentContactResponse response = parentContactService.updateParentContact(contactId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getIsPrimary()).isFalse();

        // Should NOT validate primary constraint when changing FROM primary TO non-primary
        verify(parentContactRepository, never()).findByStudentId(any());
        verify(parentContactRepository).save(any(ParentContact.class));
    }

    // ========== Get Parent Contacts Tests ==========

    @Test
    @DisplayName("Get parent contacts by student - Success")
    void getParentContactsByStudent_Success() {
        // Arrange
        ParentContact contact1 = ParentContact.builder()
                .id(UUID.randomUUID())
                .student(student)
                .fullName("Jane Doe")
                .phoneNumber("+855123456789")
                .relationship(Relationship.MOTHER)
                .isPrimary(true)
                .build();

        ParentContact contact2 = ParentContact.builder()
                .id(UUID.randomUUID())
                .student(student)
                .fullName("John Doe Sr.")
                .phoneNumber("+855987654321")
                .relationship(Relationship.FATHER)
                .isPrimary(false)
                .build();

        when(studentRepository.existsById(studentId)).thenReturn(true);
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(Arrays.asList(contact1, contact2));

        // Act
        List<ParentContactResponse> responses = parentContactService.getParentContactsByStudent(studentId);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getFullName()).isEqualTo("Jane Doe");
        assertThat(responses.get(0).getIsPrimary()).isTrue();
        assertThat(responses.get(1).getFullName()).isEqualTo("John Doe Sr.");
        assertThat(responses.get(1).getIsPrimary()).isFalse();

        verify(studentRepository).existsById(studentId);
        verify(parentContactRepository).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Get parent contacts by student - Student not found")
    void getParentContactsByStudent_StudentNotFound() {
        // Arrange
        when(studentRepository.existsById(studentId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> parentContactService.getParentContactsByStudent(studentId))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("Student with ID " + studentId + " not found");

        verify(studentRepository).existsById(studentId);
        verify(parentContactRepository, never()).findByStudentId(any());
    }

    @Test
    @DisplayName("Get parent contacts by student - Empty list")
    void getParentContactsByStudent_EmptyList() {
        // Arrange
        when(studentRepository.existsById(studentId)).thenReturn(true);
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(Collections.emptyList());

        // Act
        List<ParentContactResponse> responses = parentContactService.getParentContactsByStudent(studentId);

        // Assert
        assertThat(responses).isEmpty();

        verify(studentRepository).existsById(studentId);
        verify(parentContactRepository).findByStudentId(studentId);
    }

    @Test
    @DisplayName("Get parent contact by ID - Success")
    void getParentContactById_Success() {
        // Arrange
        when(parentContactRepository.findById(contactId)).thenReturn(Optional.of(parentContact));

        // Act
        ParentContactResponse response = parentContactService.getParentContactById(contactId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(contactId);
        assertThat(response.getFullName()).isEqualTo("Jane Doe");

        verify(parentContactRepository).findById(contactId);
    }

    @Test
    @DisplayName("Get parent contact by ID - Not found")
    void getParentContactById_NotFound() {
        // Arrange
        when(parentContactRepository.findById(contactId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> parentContactService.getParentContactById(contactId))
                .isInstanceOf(ParentContactNotFoundException.class)
                .hasMessageContaining("Parent contact with ID " + contactId + " not found");

        verify(parentContactRepository).findById(contactId);
    }

    // ========== Delete Parent Contact Tests ==========

    @Test
    @DisplayName("Delete parent contact - Success when multiple contacts exist")
    void deleteParentContact_Success() {
        // Arrange
        ParentContact contact2 = ParentContact.builder()
                .id(UUID.randomUUID())
                .student(student)
                .fullName("John Doe Sr.")
                .phoneNumber("+855987654321")
                .relationship(Relationship.FATHER)
                .isPrimary(false)
                .build();

        when(parentContactRepository.findById(contactId)).thenReturn(Optional.of(parentContact));
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(Arrays.asList(parentContact, contact2));

        // Act
        parentContactService.deleteParentContact(contactId);

        // Assert
        verify(parentContactRepository).findById(contactId);
        verify(parentContactRepository).findByStudentId(studentId);
        verify(parentContactRepository).delete(parentContact);
    }

    @Test
    @DisplayName("Delete parent contact - Fails when it's the last contact")
    void deleteParentContact_FailsWhenLastContact() {
        // Arrange
        when(parentContactRepository.findById(contactId)).thenReturn(Optional.of(parentContact));
        when(parentContactRepository.findByStudentId(studentId)).thenReturn(Collections.singletonList(parentContact));

        // Act & Assert
        assertThatThrownBy(() -> parentContactService.deleteParentContact(contactId))
                .isInstanceOf(InvalidStudentDataException.class)
                .hasMessageContaining("At least one parent contact must remain for the student");

        verify(parentContactRepository).findById(contactId);
        verify(parentContactRepository).findByStudentId(studentId);
        verify(parentContactRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete parent contact - Contact not found")
    void deleteParentContact_NotFound() {
        // Arrange
        when(parentContactRepository.findById(contactId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> parentContactService.deleteParentContact(contactId))
                .isInstanceOf(ParentContactNotFoundException.class)
                .hasMessageContaining("Parent contact with ID " + contactId + " not found");

        verify(parentContactRepository).findById(contactId);
        verify(parentContactRepository, never()).delete(any());
    }
}
