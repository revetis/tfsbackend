package com.example.apps.auth.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.apps.auth.dtos.UserLoginDTO;
import com.example.apps.auth.dtos.UserLoginDTOIU;
import com.example.apps.auth.dtos.UserRegisterDTO;
import com.example.apps.auth.dtos.UserRegisterDTOIU;
import com.example.apps.auth.entities.Role;
import com.example.apps.auth.entities.User;
import com.example.apps.auth.repositories.IRoleRepository;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.apps.auth.securities.JWTGenerator;
import com.example.settings.exceptions.UserAlreadyExistsException;
import com.example.settings.exceptions.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTGenerator jwtGenerator;

    @InjectMocks
    private UserService userService;

    private UserRegisterDTOIU registerRequest;
    private UserLoginDTOIU loginRequest;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        registerRequest = new UserRegisterDTOIU();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setPasswordRetry("password");
        registerRequest.setEmail("test@example.com");
        registerRequest.setAcceptTerms(true);
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        loginRequest = new UserLoginDTOIU();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        role = new Role();
        role.setName("USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setEnabled(true);
        user.setRoles(List.of(role));
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserRegisterDTO result = userService.registerUser(registerRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UserAlreadyExists() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsernameOrEmail(anyString(), any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtGenerator.generateRefreshToken(anyString(), anyList())).thenReturn("refreshToken");
        when(jwtGenerator.generateAccessToken(anyString())).thenReturn("accessToken");

        UserLoginDTO result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals("refreshToken", result.getRefreshToken());
        assertEquals("accessToken", result.getAccessToken());
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByUsernameOrEmail(anyString(), any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.login(loginRequest));
    }
}
