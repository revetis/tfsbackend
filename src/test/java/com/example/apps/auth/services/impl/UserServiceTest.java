package com.example.apps.auth.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.apps.auths.dtos.UserLoginDTO;
import com.example.apps.auths.dtos.UserLoginDTOIU;
import com.example.apps.auths.dtos.UserRegisterDTO;
import com.example.apps.auths.dtos.UserRegisterDTOIU;
import com.example.apps.auths.entities.Role;
import com.example.apps.auths.entities.User;
import com.example.apps.auths.repositories.IRoleRepository;
import com.example.apps.auths.repositories.IUserRepository;
import com.example.apps.auths.securities.JWTGenerator;
import com.example.apps.auths.services.impl.UserService;
import com.example.apps.notifications.services.IN8NService;
import com.example.tfs.ApplicationProperties;
import com.example.tfs.exceptions.UserAlreadyExistsException;
import com.example.tfs.exceptions.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    String FAKE_IP_ADDRESS = "127.0.0.1";

    @Mock
    private IUserRepository userRepository;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IN8NService n8NService;

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
        when(jwtGenerator.generateRefreshToken(anyString(), anyList(), anyString()))
                .thenReturn("refreshToken");
        when(jwtGenerator.generateAccessToken(anyString(), anyString()))
                .thenReturn(
                        Map.of(
                                "accessToken", "accessToken",
                                "refreshToken", "refreshToken"));

        UserLoginDTO result = userService.login(loginRequest, FAKE_IP_ADDRESS);

        assertNotNull(result);
        assertEquals("refreshToken", result.getRefreshToken());
        assertEquals("accessToken", result.getAccessToken());
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByUsernameOrEmail(anyString(), any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.login(loginRequest, FAKE_IP_ADDRESS));
    }
}
