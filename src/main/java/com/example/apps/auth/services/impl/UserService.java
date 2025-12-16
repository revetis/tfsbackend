package com.example.apps.auth.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.apps.auth.dtos.AddressDTO;
import com.example.apps.auth.dtos.ForgotPasswordDTOIU;
import com.example.apps.auth.dtos.ForgotPasswordSetNewPasswordDTOIU;
import com.example.apps.auth.dtos.ResetPasswordDTOIU;
import com.example.apps.auth.dtos.RoleDTO;
import com.example.apps.auth.dtos.UserDTO;
import com.example.apps.auth.dtos.UserDTOCreate;
import com.example.apps.auth.dtos.UserDTOUpdate;
import com.example.apps.auth.dtos.UserLoginDTO;
import com.example.apps.auth.dtos.UserLoginDTOIU;
import com.example.apps.auth.dtos.UserRegisterDTO;
import com.example.apps.auth.dtos.UserRegisterDTOIU;
import com.example.apps.auth.dtos.UserUpdateDTOIU;
import com.example.apps.auth.entities.Address;
import com.example.apps.auth.entities.ForgotPasswordToken;
import com.example.apps.auth.entities.Role;
import com.example.apps.auth.entities.User;
import com.example.apps.auth.entities.VerifyEmailToken;
import com.example.apps.auth.repositories.IForgotPasswordTokenRepository;
import com.example.apps.auth.repositories.IRoleRepository;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.apps.auth.repositories.IVerifyEmailRepository;
import com.example.apps.auth.securities.JWTGenerator;
import com.example.apps.auth.securities.JWTTokenBlacklistService;
import com.example.apps.auth.services.IUserService;
import com.example.apps.notification.services.IN8NService;
import com.example.apps.wishlists.entities.WishList;
import com.example.settings.ApplicationProperties;
import com.example.settings.exceptions.ForgotPasswordTokenIsInvalidException;
import com.example.settings.exceptions.InvalidPasswordException;
import com.example.settings.exceptions.RoleNotFoundException;
import com.example.settings.exceptions.UserAlreadyExistsException;
import com.example.settings.exceptions.UserNotAcceptedTermsException;
import com.example.settings.exceptions.UserNotFoundException;
import com.example.settings.exceptions.VerifyEmailTokenException;

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
    private IForgotPasswordTokenRepository forgotPasswordTokenRepository;

    @Autowired
    private IVerifyEmailRepository verifyEmailRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private IN8NService n8NService;

    @Override
    @Transactional
    public UserRegisterDTO registerUser(UserRegisterDTOIU request) {

        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if (user.isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        if (!request.getPassword().equals(request.getPasswordRetry())) {
            throw new InvalidPasswordException("Passwords do not match");
        }

        if (!request.getPassword()
                .matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#])[A-Za-z\\d@$!%*?&.#]{8,}$")) {
            throw new InvalidPasswordException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character, and minimum 8 characters long.");
        }

        if (!request.getAcceptTerms())

        {
            throw new UserNotAcceptedTermsException("The user must accept the terms and conditions.");
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(
                        () -> new RoleNotFoundException("User Role not found, please contact the backend developers"));
        User newUser = new User();
        WishList wishList = new WishList();
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
        newUser.setWishlist(wishList);
        newUser.setEnabled(true);
        newUser.setRoles(new ArrayList<>(List.of(role)));

        wishList.setUser(newUser);

        userRepository.save(newUser);
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", newUser.getUsername());
        payload.put("email", newUser.getEmail());
        payload.put("firstName", newUser.getFirstName());
        payload.put("lastName", newUser.getLastName());
        payload.put("phoneNumber", newUser.getPhoneNumber());
        payload.put("gender", newUser.getGender());
        payload.put("avatarUrl", newUser.getAvatarUrl());
        payload.put("enabled", newUser.getEnabled());
        payload.put("emailVerified", newUser.getEmailVerified());
        payload.put("createdAt", newUser.getCreatedAt());
        payload.put("updatedAt", newUser.getUpdatedAt());
        n8NService.triggerWorkflow(applicationProperties.getN8N_BASE_URL() + "webhook/sign-up", payload);
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
    @Cacheable(value = "users", key = "#userId")
    public UserDTO profile(String username, Long userId) throws AccessDeniedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (!currentUsername.equals(username)) {
            throw new AccessDeniedException("Access denied");
        }

        User userInfo = userRepository.findById(userId)
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
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public void avatar(MultipartFile file, Long userId) {
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 2 MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPG and PNG files are allowed");
        }

        User user = userRepository.findById(userId)
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
    @Transactional
    @CacheEvict(value = "users", key = "#result.id")
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
            throw new UserNotFoundException("User not found");
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

        Map<String, Object> paylaod = new HashMap<>();
        paylaod.put("token", token);
        paylaod.put("firstName", user.getFirstName());
        paylaod.put("email", user.getEmail());
        paylaod.put("preHeader", "Sifrenizi sıfırlamak için tıklayınız");
        paylaod.put("subject", "Sifrenizi sıfırlamak için tıklayınız");
        paylaod.put("baseURL", applicationProperties.getURL());
        paylaod.put("targetURL",
                applicationProperties.getFRONTEND_URL() + "forgot-password/reset?token=" + token);

        n8NService.triggerWorkflow(applicationProperties.getN8N_BASE_URL() + "webhook/forgot-password", paylaod);
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

    @Transactional
    @Override
    public void sendVerifyEmail(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getEmailVerified()) {
            throw new UserAlreadyExistsException("User already verified");
        }
        if (user.getVerifyEmailToken() != null && user.getVerifyEmailToken().getExpiresAt().after(new Date())) {
            throw new VerifyEmailTokenException("User already has a verify email token, please check your email");
        }

        verifyEmailRepository.deleteAllByUserId(user.getId());
        verifyEmailRepository.flush();

        user.setVerifyEmailToken(null);

        String token = UUID.randomUUID().toString();
        VerifyEmailToken verifyEmailToken = new VerifyEmailToken();
        verifyEmailToken.setUser(user);
        verifyEmailToken.setToken(token);
        verifyEmailToken.setExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 5));
        user.setVerifyEmailToken(verifyEmailToken);

        userRepository.save(user);

        Map<String, Object> paylaod = new HashMap<>();
        paylaod.put("token", token);
        paylaod.put("firstName", user.getFirstName());
        paylaod.put("email", user.getEmail());
        paylaod.put("preHeader", "E-posta adresini doğrulayın");
        paylaod.put("subject", "E-posta adresini doğrulayın");
        paylaod.put("baseURL", applicationProperties.getURL());
        paylaod.put("targetURL", applicationProperties.getFRONTEND_URL() + "verify-email?token=" + token);

        n8NService.triggerWorkflow(applicationProperties.getN8N_BASE_URL() + "webhook/verify-email", paylaod);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDTO).toList();
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTOUpdate request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setBirthOfDate(request.getBirthOfDate());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    public UserDTO createUser(UserDTOCreate request) {
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setGender(request.getGender());
        newUser.setBirthOfDate(request.getBirthOfDate());
        newUser.setAcceptTerms(true); // Admin created users are assumed to accept terms? Or maybe not needed.
        newUser.setEmailVerified(true); // Admin created users are verified?
        newUser.setEnabled(true);

        List<Role> roles = new ArrayList<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + roleId));
                roles.add(role);
            }
        } else {
            Role role = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RoleNotFoundException("Default USER Role not found"));
            roles.add(role);
        }
        newUser.setRoles(roles);

        userRepository.save(newUser);
        return convertToDTO(newUser);
    }

    @Override
    public void putUserRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    @Override
    public void deleteUserRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
            userRepository.save(user);
        }
    }

    @Override
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        List<AddressDTO> addressDTOs = new ArrayList<>();
        if (user.getAddresses() != null) {
            for (Address address : user.getAddresses()) {
                AddressDTO addressDTO = new AddressDTO();
                BeanUtils.copyProperties(address, addressDTO);
                addressDTOs.add(addressDTO);
            }
        }

        List<RoleDTO> roleDTOs = new ArrayList<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setId(role.getId());
                roleDTO.setName(role.getName());
                roleDTOs.add(roleDTO);
            }
        }

        userDTO.setAddresses(addressDTOs);
        userDTO.setRoles(roleDTOs);
        return userDTO;
    }

}
