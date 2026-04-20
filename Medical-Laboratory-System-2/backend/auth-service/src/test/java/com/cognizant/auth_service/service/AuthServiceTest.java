package com.cognizant.auth_service.service;

import com.cognizant.auth_service.dto.AuthRequest;
import com.cognizant.auth_service.dto.AuthResponse;
import com.cognizant.auth_service.entity.Role;
import com.cognizant.auth_service.entity.Users;
import com.cognizant.auth_service.repository.UserRepository;
import com.cognizant.auth_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;
    private Users testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        testUser = new Users();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword123");
        testUser.setRole(Role.PATIENT);
    }

    // ========== REGISTER TESTS ==========

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(Users.class))).thenReturn(testUser);

        // Act
        String result = authService.register(authRequest);

        // Assert
        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).encode(authRequest.getPassword());
        verify(userRepository, times(1)).save(any(Users.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterUserAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(authRequest);
        });

        assertEquals("Username Already Exists", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    @DisplayName("Should trim username before registration")
    void testRegisterTrimsUsername() {
        // Arrange
        authRequest.setUsername("  testuser  ");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(Users.class))).thenReturn(testUser);

        // Act
        authService.register(authRequest);

        // Assert
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should trim password before registration")
    void testRegisterTrimsPassword() {
        // Arrange
        authRequest.setPassword("  password123  ");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(Users.class))).thenReturn(testUser);

        // Act
        authService.register(authRequest);

        // Assert
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("Should set role as PATIENT for new users")
    void testRegisterSetsPatientRole() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users user = invocation.getArgument(0);
            assertEquals(Role.PATIENT, user.getRole());
            return user;
        });

        // Act
        authService.register(authRequest);

        // Assert
        verify(userRepository, times(1)).save(any(Users.class));
    }

    // ========== LOGIN TESTS ==========

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "PATIENT")).thenReturn("jwt_token_123");

        // Act
        AuthResponse response = authService.login(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt_token_123", response.getToken());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
        verify(jwtUtil, times(1)).generateToken("testuser", "PATIENT");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testLoginUserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(authRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception with invalid password")
    void testLoginInvalidPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(authRequest);
        });

        assertEquals("Invalid Credentials", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should trim username before login")
    void testLoginTrimsUsername() {
        // Arrange
        authRequest.setUsername("  testuser  ");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "PATIENT")).thenReturn("jwt_token_123");

        // Act
        authService.login(authRequest);

        // Assert
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should trim password before login")
    void testLoginTrimsPassword() {
        // Arrange
        authRequest.setPassword("  password123  ");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "PATIENT")).thenReturn("jwt_token_123");

        // Act
        authService.login(authRequest);

        // Assert
        verify(passwordEncoder).matches("password123", testUser.getPassword());
    }

    @Test
    @DisplayName("Should generate JWT token with correct username and role")
    void testLoginGeneratesTokenWithCorrectClaims() {
        // Arrange
        testUser.setRole(Role.LAB_TECH);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "LAB_TECH")).thenReturn("jwt_token_456");

        // Act
        AuthResponse response = authService.login(authRequest);

        // Assert
        assertEquals("jwt_token_456", response.getToken());
        verify(jwtUtil).generateToken("testuser", "LAB_TECH");
    }

    @Test
    @DisplayName("Should handle multiple login attempts")
    void testMultipleLoginAttempts() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "PATIENT"))
                .thenReturn("jwt_token_1")
                .thenReturn("jwt_token_2")
                .thenReturn("jwt_token_3");

        // Act - Login 3 times
        AuthResponse response1 = authService.login(authRequest);
        AuthResponse response2 = authService.login(authRequest);
        AuthResponse response3 = authService.login(authRequest);

        // Assert - Each token should be different (or same based on implementation)
        assertNotNull(response1.getToken());
        assertNotNull(response2.getToken());
        assertNotNull(response3.getToken());
        verify(jwtUtil, times(3)).generateToken("testuser", "PATIENT");
    }

}

