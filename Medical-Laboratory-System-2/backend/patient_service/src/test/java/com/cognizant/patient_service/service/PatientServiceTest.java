package com.cognizant.patient_service.service;

import com.cognizant.patient_service.dto.PatientRequest;
import com.cognizant.patient_service.entity.Patient;
import com.cognizant.patient_service.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("PatientService Unit Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientRequest patientRequest;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        patientRequest = new PatientRequest();
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setAge(30);
        patientRequest.setGender("Male");
        patientRequest.setPhoneNumber("9876543210");
        patientRequest.setEmail("john@example.com");
        patientRequest.setAddress("123 Main St, City");

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setUsername("johndoe");
        testPatient.setFirstname("John");
        testPatient.setLastname("Doe");
        testPatient.setAge(30);
        testPatient.setGender("Male");
        testPatient.setPhone("9876543210");
        testPatient.setEmail("john@example.com");
        testPatient.setAddress("123 Main St, City");
        testPatient.setCreatedAt(LocalDateTime.now());
    }

    // ========== CREATE PROFILE TESTS ==========

    @Test
    @DisplayName("Should create profile successfully for new user")
    void testCreateProfileSuccess() {
        // Arrange
        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        String result = patientService.createProfile("johndoe", patientRequest);

        // Assert
        assertEquals("Profile created successfully.", result);
        verify(patientRepository, times(1)).findByUsername("johndoe");
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should return message when profile already exists")
    void testCreateProfileAlreadyExists() {
        // Arrange
        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.of(testPatient));

        // Act
        String result = patientService.createProfile("johndoe", patientRequest);

        // Assert
        assertEquals("Profile already exists for this user.", result);
        verify(patientRepository, times(1)).findByUsername("johndoe");
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should set all patient fields correctly on create")
    void testCreateProfileSetsAllFields() {
        // Arrange
        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            assertEquals("johndoe", patient.getUsername());
            assertEquals("John", patient.getFirstname());
            assertEquals("Doe", patient.getLastname());
            assertEquals(30, patient.getAge());
            assertEquals("Male", patient.getGender());
            assertEquals("9876543210", patient.getPhone());
            assertEquals("john@example.com", patient.getEmail());
            assertEquals("123 Main St, City", patient.getAddress());
            assertNotNull(patient.getCreatedAt());
            return patient;
        });

        // Act
        patientService.createProfile("johndoe", patientRequest);

        // Assert
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should set createdAt timestamp on profile creation")
    void testCreateProfileSetsCreatedAtTimestamp() {
        // Arrange
        LocalDateTime beforeCreate = LocalDateTime.now();
        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            assertNotNull(patient.getCreatedAt());
            assertTrue(patient.getCreatedAt().isAfter(beforeCreate) || patient.getCreatedAt().isEqual(beforeCreate));
            return patient;
        });

        // Act
        patientService.createProfile("johndoe", patientRequest);

        // Assert
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    // ========== GET PROFILE TESTS ==========

    @Test
    @DisplayName("Should retrieve patient profile by username successfully")
    void testGetProfileSuccess() {
        // Arrange
        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.of(testPatient));

        // Act
        Patient result = patientService.getProfile("johndoe");

        // Assert
        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        assertEquals("John", result.getFirstname());
        assertEquals("Doe", result.getLastname());
        verify(patientRepository, times(1)).findByUsername("johndoe");
    }

    @Test
    @DisplayName("Should throw exception when profile not found by username")
    void testGetProfileNotFound() {
        // Arrange
        when(patientRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.getProfile("nonexistent");
        });

        assertEquals("Profile not found for user: nonexistent", exception.getMessage());
        verify(patientRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should return correct patient details on get profile")
    void testGetProfileReturnsCorrectData() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(5L);
        patient.setUsername("janedoe");
        patient.setFirstname("Jane");
        patient.setLastname("Smith");
        patient.setAge(28);
        patient.setGender("Female");
        patient.setPhone("9123456789");
        patient.setEmail("jane@example.com");
        patient.setAddress("456 Oak Ave, Town");

        when(patientRepository.findByUsername("janedoe")).thenReturn(Optional.of(patient));

        // Act
        Patient result = patientService.getProfile("janedoe");

        // Assert
        assertEquals(5L, result.getId());
        assertEquals("Jane", result.getFirstname());
        assertEquals("Smith", result.getLastname());
        assertEquals(28, result.getAge());
        assertEquals("Female", result.getGender());
    }

    // ========== GET PATIENT BY ID TESTS ==========

    @Test
    @DisplayName("Should retrieve patient by ID successfully")
    void testGetPatientByIdSuccess() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act
        Patient result = patientService.getPatientById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("johndoe", result.getUsername());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when patient not found by ID")
    void testGetPatientByIdNotFound() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.getPatientById(999L);
        });

        assertEquals("Patient not found: 999", exception.getMessage());
        verify(patientRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should return correct patient details on get by ID")
    void testGetPatientByIdReturnsCorrectData() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(100L);
        patient.setUsername("testuser");
        patient.setFirstname("Test");
        patient.setLastname("User");
        patient.setAge(35);

        when(patientRepository.findById(100L)).thenReturn(Optional.of(patient));

        // Act
        Patient result = patientService.getPatientById(100L);

        // Assert
        assertEquals(100L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test", result.getFirstname());
    }

    // ========== UPDATE PROFILE TESTS ==========

    @Test
    @DisplayName("Should update patient profile successfully")
    void testUpdateProfileSuccess() {
        // Arrange
        Patient existingPatient = new Patient();
        existingPatient.setId(1L);
        existingPatient.setUsername("johndoe");
        existingPatient.setFirstname("Old");
        existingPatient.setLastname("Name");

        PatientRequest updateRequest = new PatientRequest();
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Updated");
        updateRequest.setAge(31);
        updateRequest.setGender("Male");
        updateRequest.setPhoneNumber("1234567890");
        updateRequest.setEmail("john.updated@example.com");
        updateRequest.setAddress("New Address");

        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(existingPatient);

        // Act
        String result = patientService.updateProfile("johndoe", updateRequest);

        // Assert
        assertEquals("Profile updated successfully.", result);
        verify(patientRepository, times(1)).findByUsername("johndoe");
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent profile")
    void testUpdateProfileNotFound() {
        // Arrange
        when(patientRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.updateProfile("nonexistent", patientRequest);
        });

        assertEquals("Profile not found for user: nonexistent", exception.getMessage());
        verify(patientRepository, times(1)).findByUsername("nonexistent");
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should update all patient fields correctly")
    void testUpdateProfileUpdatesAllFields() {
        // Arrange
        Patient existingPatient = new Patient();
        existingPatient.setId(1L);
        existingPatient.setUsername("johndoe");

        PatientRequest updateRequest = new PatientRequest();
        updateRequest.setFirstName("UpdatedFirst");
        updateRequest.setLastName("UpdatedLast");
        updateRequest.setAge(32);
        updateRequest.setGender("Other");
        updateRequest.setPhoneNumber("5555555555");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setAddress("Updated Address");

        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            assertEquals("UpdatedFirst", patient.getFirstname());
            assertEquals("UpdatedLast", patient.getLastname());
            assertEquals(32, patient.getAge());
            assertEquals("Other", patient.getGender());
            assertEquals("5555555555", patient.getPhone());
            assertEquals("updated@example.com", patient.getEmail());
            assertEquals("Updated Address", patient.getAddress());
            assertNotNull(patient.getUpdatedAt());
            return patient;
        });

        // Act
        patientService.updateProfile("johndoe", updateRequest);

        // Assert
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should set updatedAt timestamp on profile update")
    void testUpdateProfileSetsUpdatedAtTimestamp() {
        // Arrange
        Patient existingPatient = new Patient();
        existingPatient.setId(1L);
        existingPatient.setUsername("johndoe");

        LocalDateTime beforeUpdate = LocalDateTime.now();
        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            assertNotNull(patient.getUpdatedAt());
            assertTrue(patient.getUpdatedAt().isAfter(beforeUpdate) || patient.getUpdatedAt().isEqual(beforeUpdate));
            return patient;
        });

        // Act
        patientService.updateProfile("johndoe", patientRequest);

        // Assert
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should preserve patient ID on update")
    void testUpdateProfilePreservesId() {
        // Arrange
        Patient existingPatient = new Patient();
        existingPatient.setId(42L);
        existingPatient.setUsername("johndoe");
        existingPatient.setFirstname("Old");

        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            assertEquals(42L, patient.getId());
            return patient;
        });

        // Act
        patientService.updateProfile("johndoe", patientRequest);

        // Assert
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle null values in PatientRequest gracefully")
    void testCreateProfileWithNullValues() {
        // Arrange
        PatientRequest nullRequest = new PatientRequest();
        nullRequest.setFirstName(null);
        nullRequest.setLastName(null);

        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act - Should not throw exception
        String result = patientService.createProfile("johndoe", nullRequest);

        // Assert
        assertEquals("Profile created successfully.", result);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should handle empty string username gracefully")
    void testGetProfileWithEmptyUsername() {
        // Arrange
        when(patientRepository.findByUsername("")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            patientService.getProfile("");
        });

        assertEquals("Profile not found for user: ", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle multiple profile updates in sequence")
    void testMultipleProfileUpdates() {
        // Arrange
        Patient existingPatient = new Patient();
        existingPatient.setId(1L);
        existingPatient.setUsername("johndoe");
        existingPatient.setAge(30);

        PatientRequest request1 = new PatientRequest();
        request1.setFirstName("John");
        request1.setLastName("Doe");
        request1.setAge(31);
        request1.setGender("Male");
        request1.setPhoneNumber("9876543210");
        request1.setEmail("john@example.com");
        request1.setAddress("Address 1");

        PatientRequest request2 = new PatientRequest();
        request2.setFirstName("John");
        request2.setLastName("Doe");
        request2.setAge(32);
        request2.setGender("Male");
        request2.setPhoneNumber("9876543210");
        request2.setEmail("john@example.com");
        request2.setAddress("Address 2");

        when(patientRepository.findByUsername("johndoe")).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(existingPatient);

        // Act - Update twice
        String result1 = patientService.updateProfile("johndoe", request1);
        String result2 = patientService.updateProfile("johndoe", request2);

        // Assert
        assertEquals("Profile updated successfully.", result1);
        assertEquals("Profile updated successfully.", result2);
        verify(patientRepository, times(2)).save(any(Patient.class));
    }

}

