package com.example.apps.auth.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.apps.auth.dto.AddressDTO;
import com.example.apps.auth.dto.ForgotPasswordDTOIU;
import com.example.apps.auth.dto.ForgotPasswordSetNewPasswordDTOIU;
import com.example.apps.auth.dto.ResetPasswordDTOIU;
import com.example.apps.auth.dto.RoleDTO;
import com.example.apps.auth.dto.UserDTO;
import com.example.apps.auth.dto.UserLoginDTO;
import com.example.apps.auth.dto.UserLoginDTOIU;
import com.example.apps.auth.dto.UserRegisterDTO;
import com.example.apps.auth.dto.UserRegisterDTOIU;
import com.example.apps.auth.dto.UserUpdateDTOIU;
import com.example.apps.auth.entities.Address;
import com.example.apps.auth.entities.ForgotPasswordToken;
import com.example.apps.auth.entities.Role;
import com.example.apps.auth.entities.User;
import com.example.apps.auth.entities.VerifyEmailToken;
import com.example.apps.auth.repositories.IForgotPasswordTokenRepository;
import com.example.apps.auth.repositories.IRoleRepository;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.apps.auth.repositories.IVerifyEmailRepository;
import com.example.apps.auth.security.JWTGenerator;
import com.example.apps.auth.security.JWTTokenBlacklistService;
import com.example.apps.auth.services.IUserService;
import com.example.settings.EmailService;
import com.example.settings.exceptions.ForgotPasswordTokenIsInvalidException;
import com.example.settings.exceptions.InvalidPasswordException;
import com.example.settings.exceptions.RoleNotFoundException;
import com.example.settings.exceptions.UserAlreadyExistsException;
import com.example.settings.exceptions.UserNotAcceptedTermsException;
import com.example.settings.exceptions.UserNotFoundException;
import com.example.settings.exceptions.VerifyEmailTokenException;

import jakarta.transaction.Transactional;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private JWTTokenBlacklistService jwtTokenBlacklistService;

    @Autowired
    private EmailService EmailService;

    @Autowired
    private IForgotPasswordTokenRepository forgotPasswordTokenRepository;

    @Autowired
    private IVerifyEmailRepository verifyEmailRepository;

    @Override
    public UserRegisterDTO registerUser(UserRegisterDTOIU request) {

        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if (user.isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        if (!request.getPassword().equals(request.getPasswordRetry())) {
            throw new InvalidPasswordException("Passwords do not match");
        }

        if (!request.getAcceptTerms()) {
            throw new UserNotAcceptedTermsException("The user must accept the terms and conditions.");
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(
                        () -> new RoleNotFoundException("User Role not found, please contact the backend developers"));
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setAvatarUrl(request.getAvatarUrl());
        newUser.setGender(request.getGender());
        newUser.setAcceptTerms(request.getAcceptTerms());
        newUser.setEmailVerified(false);
        newUser.setEnabled(true);
        newUser.setRoles(List.of(role));

        userRepository.save(newUser);

        return new UserRegisterDTO(newUser.getUsername());
    }

    @Override
    public UserLoginDTO login(UserLoginDTOIU request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }
        if (!user.getEnabled()) {
            throw new UserNotFoundException("User not found");
        }

        String refreshToken = jwtGenerator.generateRefreshToken(user.getUsername(),
                user.getRoles().stream().map(role -> role.getName()).toList());

        String accessToken = jwtGenerator.generateAccessToken(refreshToken);

        return new UserLoginDTO(refreshToken, accessToken);
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    public UserDTO profile(String username) throws AccessDeniedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (!currentUsername.equals(username)) {
            throw new AccessDeniedException("Access denied");
        }

        User userInfo = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserDTO userDTO = new UserDTO();

        BeanUtils.copyProperties(userInfo, userDTO);

        // Her bir List<adress> ve List<Role> objesini List<Role||AddressDTO> objesine
        // donusturuyoruz
        List<AddressDTO> addressDTOs = new ArrayList<>();
        for (Address address : userInfo.getAddresses()) {
            AddressDTO addressDTO = new AddressDTO();
            BeanUtils.copyProperties(address, addressDTO);
            addressDTOs.add(addressDTO);
        }
        List<RoleDTO> roleDTOs = new ArrayList<>();

        for (Role role : userInfo.getRoles()) {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setName(role.getName());
            roleDTOs.add(roleDTO);
        }
        userDTO.setAddresses(addressDTOs);
        userDTO.setRoles(roleDTOs);

        return userDTO;
    }

    @Override
    @CacheEvict(value = "users", key = "#principal.name")
    public void avatar(MultipartFile file, java.security.Principal principal) {
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 2 MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPG and PNG files are allowed");
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        java.nio.file.Path uploadPath = Paths.get("uploads/avatars");

        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (Exception e) {
                throw new RuntimeException("Error creating upload directory", e);
            }
        }

        try {
            Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            System.getLogger(UserService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }

        user.setAvatarUrl("/uploads/avatars/" + filename);
        userRepository.save(user);
    }

    @Override
    public String logout(String accessToken, String refreshToken) {
        jwtTokenBlacklistService.accessTokenBlacklist(accessToken);
        jwtTokenBlacklistService.refreshTokenBlacklist(refreshToken);
        return refreshToken;
    }

    @Override
    @CacheEvict(value = "users", key = "#principal.name")
    public UserDTO updateProfile(UserUpdateDTOIU request, Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new UserAlreadyExistsException("User already exists with this email");
                }
            });
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
        }

        if (!user.getPhoneNumber().equals(request.getPhoneNumber())) {
            userRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new UserAlreadyExistsException("User already exists with this phone number");
                }
            });
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBirthOfDate(request.getBirthOfDate());

        User updatedUser = userRepository.save(user);

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(updatedUser, userDTO);

        return userDTO;
    }

    @Override
    public void resetPassword(ResetPasswordDTOIU request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid current password");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password can't be same as current password");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordRetry())) {
            throw new InvalidPasswordException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

    }

    @Override
    public void forgotPassword(ForgotPasswordDTOIU request, ForgotPasswordSetNewPasswordDTOIU newPassword,
            String... args) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        String parToken = null;
        if (args.length > 0 && args[0] != null) {
            parToken = args[0];
        }

        if (parToken != null) {
            ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findByToken(parToken);
            if (forgotPasswordToken == null) {
                throw new ForgotPasswordTokenIsInvalidException("Invalid token");
            }
            User userSetPass = forgotPasswordToken.getUser();
            forgotPasswordSetNewPassword(userSetPass, newPassword, parToken);
            return;
        }
        if (user == null) {
            return;
        }

        String token = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken();
        forgotPasswordToken.setUser(user);
        forgotPasswordToken.setToken(token);
        forgotPasswordToken.setExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
        user.setForgotPasswordToken(forgotPasswordToken);

        userRepository.save(user);

        sendForgotPasswordLink(user, token);

    }

    private void forgotPasswordSetNewPassword(User user, ForgotPasswordSetNewPasswordDTOIU newPassword, String token) {
        ForgotPasswordToken forgotPasswordToken = user.getForgotPasswordToken();
        if (forgotPasswordToken == null) {
            return;
        }
        if (!forgotPasswordToken.getToken().equals(token)) {
            return;
        }
        if (forgotPasswordToken.getExpiresAt().before(new Date())) {
            return;
        }
        if (!newPassword.getPassword().equals(newPassword.getPasswordRetry())) {
            throw new InvalidPasswordException("Passwords do not match");
        }
        if (passwordEncoder.matches(newPassword.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password can't be same as current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword.getPassword()));
        user.setForgotPasswordToken(null);
        userRepository.save(user);
    }

    private void sendForgotPasswordLink(User user, String token) {

        EmailService.send(user.getEmail(), "TFS - ForgotPassword",
                "Reset password: https://localhost/forgot-password?token=" + token);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerifyEmailToken verifyEmailToken = verifyEmailRepository.findByToken(token)
                .orElseThrow(() -> new VerifyEmailTokenException("Token not found"));

        User user = verifyEmailToken.getUser();
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        if (user.getEmailVerified()) {
            throw new UserAlreadyExistsException("User already verified");
        }
        if (verifyEmailToken.getExpiresAt().before(new Date())) {
            throw new VerifyEmailTokenException("Token expired");
        }

        verifyEmailRepository.delete(verifyEmailToken);
        user.setVerifyEmailToken(null);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

}
