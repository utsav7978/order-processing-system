package com.orderplatform.user.service;

import com.orderplatform.user.dto.JwtResponse;
import com.orderplatform.user.dto.LoginRequest;
import com.orderplatform.user.dto.RegisterRequest;
import com.orderplatform.user.dto.UserResponse;
import com.orderplatform.user.entity.Role;
import com.orderplatform.user.entity.User;
import com.orderplatform.user.exception.DuplicateResourceException;
import com.orderplatform.user.exception.InvalidCredentialsException;
import com.orderplatform.user.mapper.UserMapper;
import com.orderplatform.user.repository.UserRepository;
import com.orderplatform.user.security.JwtUtil;
import com.orderplatform.user.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_savesNewUser_whenEmailNotTaken() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toUserResponse(existingUser)).thenReturn(
                UserResponse.builder().id(1L).fullName("Jane Doe").email("jane@example.com").role(Role.USER).build());

        UserResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("jane@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_returnsJwtResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken("jane@example.com", "USER")).thenReturn("mock-jwt-token");
        when(userMapper.toJwtResponse(existingUser, "mock-jwt-token")).thenReturn(
                JwtResponse.builder().token("mock-jwt-token").type("Bearer").email("jane@example.com").role(Role.USER).build());

        JwtResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void login_throwsInvalidCredentialsException_whenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("jane@example.com", "wrong-password");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_throwsInvalidCredentialsException_whenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
