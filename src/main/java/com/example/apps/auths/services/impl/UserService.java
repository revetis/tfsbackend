package com.example.apps.auths.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.example.apps.auths.dtos.*;
import com.example.apps.audit.annotations.Auditable;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import lombok.extern.slf4j.Slf4j;
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

import com.example.apps.auths.entities.Address;
import com.example.apps.auths.entities.ForgotPasswordToken;
import com.example.apps.auths.entities.Role;
import com.example.apps.auths.entities.User;
import com.example.apps.auths.entities.VerifyEmailToken;
import com.example.apps.auths.entities.TwoFactorToken;
import com.example.apps.auths.repositories.IForgotPasswordTokenRepository;
import com.example.apps.auths.repositories.IRoleRepository;
import com.example.apps.auths.repositories.IUserRepository;
import com.example.apps.auths.repositories.IVerifyEmailRepository;
import com.example.apps.auths.securities.JWTGenerator;
import com.example.apps.auths.securities.JWTTokenBlacklistService;
import com.example.apps.auths.services.IUserService;
import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;
import com.example.tfs.ApplicationProperties;
import com.example.tfs.exceptions.ForgotPasswordTokenIsInvalidException;
import com.example.tfs.exceptions.InvalidPasswordException;
import com.example.tfs.exceptions.RoleNotFoundException;
import com.example.tfs.exceptions.UserAlreadyExistsException;
import com.example.tfs.exceptions.UserNotAcceptedTermsException;
import com.example.tfs.exceptions.UserNotFoundException;
import com.example.tfs.exceptions.VerifyEmailTokenException;

import javax.crypto.SecretKey;

@Slf4j
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

    @Autowired
    private N8NProperties n8NProperties;

    @Autowired
    private com.example.tfs.StorageService storageService;

    @Autowired
    private com.example.apps.orders.services.IOrderService orderService;

    @Autowired
    private com.example.apps.auths.repositories.TwoFactorTokenRepository twoFactorTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    @Auditable(action = "USER_REGISTER")
    public UserRegisterDTO registerUser(UserRegisterDTOIU request) {

        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if (user.isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        if (!request.getPassword().equals(request.getPasswordRetry())) {
            throw new InvalidPasswordException("Passwords do not match");
        }

        if (!request.getPassword()
                .matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$")) {
            throw new InvalidPasswordException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character, and minimum 8 characters long.");
        }

        if (!request.getAcceptTerms()) {
            throw new UserNotAcceptedTermsException("The user must accept the terms and conditions.");
        }

        // Age Validation (18+)
        if (request.getBirthOfDate() != null) {
            java.time.LocalDate birthDate = request.getBirthOfDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            java.time.LocalDate now = java.time.LocalDate.now();
            int age = java.time.Period.between(birthDate, now).getYears();
            if (age < 18) {
                throw new com.example.tfs.exceptions.UserUnderAgeException(
                        "Kayıt olmak için en az 18 yaşında olmalısınız.");
            }
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
        newUser.setBirthOfDate(request.getBirthOfDate());
        newUser.setAcceptTerms(request.getAcceptTerms());
        newUser.setEmailVerified(false);
        newUser.setEnabled(true);
        newUser.setRoles(new ArrayList<>(List.of(role)));
        newUser.setIsSubscribedToNewsletter(request.getIsSubscribedToNewsletter());

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
        n8NService.triggerWorkflow(n8NProperties.getBaseUrl() + n8NProperties.getWebhook().getSignUp(), payload);
        return new UserRegisterDTO(newUser.getUsername());
    }

    @Override
    @Transactional
    @Auditable(action = "USER_LOGIN")
    public UserLoginDTO login(UserLoginDTOIU request, String ipAddress) {

        User user = userRepository.findByEmail(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByUsername(request.getUsernameOrEmail())
                        .orElseThrow(() -> new UserNotFoundException("Invalid password or email/username")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password or email/username");
        }
        if (!user.getEnabled()) {
            throw new UserNotFoundException("Invalid password or email/username");
        }

        // 2FA Check
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            // Delete existing tokens for safety/cleanliness
            twoFactorTokenRepository.deleteAllByUserId(user.getId());
            twoFactorTokenRepository.flush();

            String code = String.format("%06d", secureRandom.nextInt(999999));
            String verificationId = UUID.randomUUID().toString();

            TwoFactorToken token = new TwoFactorToken();
            token.setCode(code);
            token.setVerificationId(verificationId);
            token.setUser(user);
            token.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(3));
            token.setAttemptCount(0);

            twoFactorTokenRepository.save(token);

            // Mask email
            String email = user.getEmail();
            String maskedEmail = email.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");

            Map<String, Object> payload = new HashMap<>();
            payload.put("code", code);
            payload.put("firstName", user.getFirstName());
            payload.put("email", user.getEmail());
            n8NService.triggerWorkflow(n8NProperties.getWebhook().getTwoFaLogin(), payload);

            UserLoginDTO loginDTO = new UserLoginDTO();
            loginDTO.setRequire2fa(true);
            loginDTO.setVerificationId(verificationId);
            loginDTO.setMaskedEmail(maskedEmail);
            return loginDTO;
        }

        String refreshToken = jwtGenerator.generateRefreshToken(user.getUsername(),
                user.getRoles().stream().map(role -> role.getName()).toList(), ipAddress);

        user.setRefreshToken(refreshToken);
        user.setLastLoginDate(new Date());
        userRepository.save(user);

        Map<String, String> tokens = jwtGenerator.generateAccessTokenForLogin(refreshToken, ipAddress);

        return new UserLoginDTO(tokens.get("refreshToken"), tokens.get("accessToken"), false, null, null);
    }

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#a1")
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
        if (userInfo.getAddresses() != null) {
            for (Address address : userInfo.getAddresses()) {
                AddressDTO addressDTO = new AddressDTO();
                BeanUtils.copyProperties(address, addressDTO);
                addressDTOs.add(addressDTO);
            }
        }
        List<RoleDTO> roleDTOs = new ArrayList<>();

        if (userInfo.getRoles() != null) {
            for (Role role : userInfo.getRoles()) {
                RoleDTO roleDTO = new RoleDTO();
                roleDTO.setName(role.getName());
                roleDTOs.add(roleDTO);
            }
        }
        userDTO.setAddresses(addressDTOs);
        userDTO.setRoles(roleDTOs);

        return userDTO;
    }

    @Override
    @CacheEvict(value = "users", key = "#a1")
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

        String filename = storageService.store(file, "avatars");
        user.setAvatarUrl("/uploads/avatars/" + filename);
        userRepository.save(user);
    }

    @Override
    public String logout(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            jwtTokenBlacklistService.accessTokenBlacklist(accessToken);
        }
        if (refreshToken != null && !refreshToken.isEmpty()) {
            jwtTokenBlacklistService.refreshTokenBlacklist(refreshToken);
        }
        return refreshToken;
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#result.id")
    @Auditable(action = "USER_PROFILE_UPDATE")
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
        user.setIsSubscribedToNewsletter(request.getIsSubscribedToNewsletter());

        User updatedUser = userRepository.save(user);

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(updatedUser, userDTO);

        return userDTO;
    }

    @Override
    @Transactional
    @Auditable(action = "USER_PASSWORD_RESET")
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
    @Transactional
    @Auditable(action = "USER_FORGOT_PASSWORD_REQUEST")
    public void forgotPassword(ForgotPasswordDTOIU request, ForgotPasswordSetNewPasswordDTOIU newPassword,
            String... args) {
        String parToken = null;
        if (args.length > 0 && args[0] != null) {
            parToken = args[0];
        }

        // Handle password reset with token first (request can be null in this case)
        if (parToken != null) {
            ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository.findByToken(parToken);
            if (forgotPasswordToken == null) {
                throw new ForgotPasswordTokenIsInvalidException("Invalid token");
            }
            User userSetPass = forgotPasswordToken.getUser();
            forgotPasswordSetNewPassword(userSetPass, newPassword, parToken);
            return;
        }

        // Handle forgot password request (email required)
        if (request == null || request.getEmail() == null) {
            throw new IllegalArgumentException("Email is required");
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        // Delete existing forgot password token if exists (orphanRemoval will handle
        // actual deletion)
        if (user.getForgotPasswordToken() != null) {
            user.setForgotPasswordToken(null);
            userRepository.saveAndFlush(user);
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

        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        payload.put("firstName", user.getFirstName());
        payload.put("email", user.getEmail());
        payload.put("preHeader", "Sifrenizi sıfırlamak için tıklayınız");
        payload.put("subject", "Sifrenizi sıfırlamak için tıklayınız");
        payload.put("baseURL", applicationProperties.getURL());
        payload.put("targetURL",
                applicationProperties.getFRONTEND_URL() + "forgot-password/reset?token=" + token);

        n8NService.triggerWorkflow(n8NProperties.getBaseUrl() + n8NProperties.getWebhook().getForgotPassword(),
                payload);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#result.id")
    public UserDTO verifyEmail(String token) {
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
        User updatedUser = userRepository.save(user);

        return convertToDTO(updatedUser);
    }

    @Transactional
    @Override
    public void sendVerifyEmail(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getEmailVerified()) {
            throw new UserAlreadyExistsException("User already verified");
        }
        // Removed blocking time check to allow resending if email was lost
        // if (user.getVerifyEmailToken() != null &&
        // user.getVerifyEmailToken().getExpiresAt().after(new Date())) {
        // throw new VerifyEmailTokenException("User already has a verify email token,
        // please check your email");
        // }

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

        n8NService.triggerWorkflow(n8NProperties.getBaseUrl() + n8NProperties.getWebhook().getVerifyEmail(), paylaod);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll(org.springframework.data.domain.Sort.by("createdAt").descending()).stream()
                .map(this::convertToSummaryDTO).toList();
    }

    @Override
    public UserPageResult getAllUsers(int start, int end, String sortField, String sortOrder, String search,
            String username, String email, Long roleId) {
        org.springframework.data.jpa.domain.Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (username != null && !username.isEmpty()) {
            spec = spec.and(
                    (root, query, cb) -> cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }
        if (email != null && !email.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (search != null && !search.isEmpty()) {
            String searchLower = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("username")), searchLower),
                    cb.like(cb.lower(root.get("email")), searchLower),
                    cb.like(cb.lower(root.get("firstName")), searchLower),
                    cb.like(cb.lower(root.get("lastName")), searchLower)));
        }

        if (roleId != null) {
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<com.example.apps.auths.entities.User> subRoot = subquery
                        .from(com.example.apps.auths.entities.User.class);
                jakarta.persistence.criteria.Join<com.example.apps.auths.entities.User, com.example.apps.auths.entities.Role> subRoleJoin = subRoot
                        .join("roles");
                subquery.select(subRoot.get("id")).where(cb.equal(subRoleJoin.get("id"), roleId));
                return root.get("id").in(subquery);
            });
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                sortOrder.equalsIgnoreCase("ASC") ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC,
                sortField);

        int pageSize = end - start;
        if (pageSize <= 0)
            pageSize = 10;
        int pageNumber = start / pageSize;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber,
                pageSize, sort);

        org.springframework.data.domain.Page<User> page = userRepository.findAll(spec, pageable);

        List<UserDTO> dtos = page.getContent().stream().map(this::convertToSummaryDTO).toList();
        return new UserPageResult(dtos, page.getTotalElements());
    }

    private UserDTO convertToSummaryDTO(User user) {
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
                BeanUtils.copyProperties(role, roleDTO);
                roleDTOs.add(roleDTO);
            }
        }

        userDTO.setAddresses(addressDTOs);
        userDTO.setRoles(roleDTOs);
        // Do not fetch orders for simple user list to avoid N+1 performance issues
        return userDTO;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserDTO getUserByUsernameOrEmail(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail).orElseGet(() -> userRepository
                .findByEmail(usernameOrEmail).orElseThrow(() -> new UserNotFoundException("User not found")));
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    @Auditable(action = "USER_UPDATE_ADMIN")
    public UserDTO updateUser(Long userId, UserDTOUpdate request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setBirthOfDate(request.getBirthOfDate());
        user.setIsSubscribedToNewsletter(request.getIsSubscribedToNewsletter());

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getRoleIds() != null) {
            List<Role> roles = new ArrayList<>();
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
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
        newUser.setIsSubscribedToNewsletter(request.getIsSubscribedToNewsletter());

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
    @Transactional
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
    @Transactional
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
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    @Auditable(action = "USER_ENABLE")
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    @Auditable(action = "USER_DISABLE")
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteMyAccount(DeleteAccountRequest request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }

        // Soft delete / Anonymize user data to preserve order history
        String uniqueSuffix = UUID.randomUUID().toString();
        user.setUsername("deleted_" + uniqueSuffix);
        user.setEmail("deleted_" + uniqueSuffix + "@example.com");
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setPhoneNumber(null); // Assuming phone number can be null, otherwise use dummy
        user.setAvatarUrl(null);
        user.setBirthOfDate(null);
        user.setGender(null);
        user.setEnabled(false);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Scramble password

        user.setRefreshToken(null);
        user.setForgotPasswordToken(null);
        user.setVerifyEmailToken(null);

        // Disable roles or set to a dummy role if needed?
        // Keeping roles might be okay if account is disabled, but safer to remove ADMIN
        // role if present
        // keeping default USER role is fine.

        // Clear addresses (personal data)
        if (user.getAddresses() != null) {
            user.getAddresses().clear();
        }

        userRepository.save(user);
    }

    /**
     * Kullanicinin yetkilerini ve izinlerini kontrol eder, !YUKSEK! seviye bir
     * methodtur
     *
     * @param refreshToken
     * @param accessToken
     * @param ipAddress
     * @return yetki kontrol eder, yetki gecersizse false dondurur
     */
    @Override
    public AccessCheckDTO accessCheck(String refreshToken, String accessToken, String ipAddress) {
        AccessCheckDTO dto = new AccessCheckDTO();
        dto.setIsPermitted(false);

        try {
            SecretKey key = applicationProperties.getJwtSigningKey();
            var parser = Jwts.parserBuilder().setSigningKey(key).build();

            // refresh token blacklist
            if (jwtTokenBlacklistService.isRefreshTokenBlacklisted(refreshToken)) {
                return dto;
            }

            // refresh token parse + IP kontrolü
            Claims refreshClaims = parser.parseClaimsJws(refreshToken).getBody();
            String refreshIp = refreshClaims.get("ipAddress", String.class);

            if (!ipAddress.equals(refreshIp)) {
                return dto;
            }

            // access token yoksa yetki yok
            if (accessToken == null || jwtTokenBlacklistService.isAccessTokenBlacklisted(accessToken)) {
                return dto;
            }

            // access token parse
            Claims accessClaims = parser.parseClaimsJws(accessToken).getBody();
            List<?> roles = accessClaims.get("roles", List.class);

            if (roles == null || !roles.contains("ROLE_ADMIN")) {
                return dto;
            }

            // access token süresi dolmuş mu
            if (accessClaims.getExpiration().before(new Date())) {
                return dto;
            }

            dto.setIsPermitted(true);
            return dto;

        } catch (Exception e) {
            log.atError().log(e.getMessage());
            return dto;
        }
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
                BeanUtils.copyProperties(role, roleDTO);
                roleDTOs.add(roleDTO);
            }
        }

        userDTO.setAddresses(addressDTOs);
        userDTO.setRoles(roleDTOs);

        if (user.getId() != null) {
            userDTO.setOrders(orderService.getByUserId(user.getId()));
        }

        return userDTO;
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void adminResetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserLoginDTO verifyTwoFactorLogin(String verificationId, String code, String ipAddress) {
        TwoFactorToken token = twoFactorTokenRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification session"));

        if (token.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }

        if (token.getAttemptCount() >= 3) {
            twoFactorTokenRepository.delete(token);
            throw new IllegalArgumentException("Too many failed attempts. Please login again.");
        }

        if (!token.getCode().equals(code)) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            twoFactorTokenRepository.save(token);
            throw new IllegalArgumentException("Invalid verification code");
        }

        User user = token.getUser();
        twoFactorTokenRepository.delete(token);

        String refreshToken = jwtGenerator.generateRefreshToken(user.getUsername(),
                user.getRoles().stream().map(role -> role.getName()).toList(), ipAddress);

        user.setRefreshToken(refreshToken);
        user.setLastLoginDate(new Date());
        userRepository.save(user);

        Map<String, String> tokens = jwtGenerator.generateAccessTokenForLogin(refreshToken, ipAddress);

        return new UserLoginDTO(tokens.get("refreshToken"), tokens.get("accessToken"), false, null, null);
    }

    @Override
    @Transactional
    public String initiateTwoFactorToggle(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        twoFactorTokenRepository.deleteAllByUserId(user.getId());
        twoFactorTokenRepository.flush();

        String code = String.format("%06d", secureRandom.nextInt(999999));
        String verificationId = UUID.randomUUID().toString();

        TwoFactorToken token = new TwoFactorToken();
        token.setCode(code);
        token.setVerificationId(verificationId);
        token.setUser(user);
        token.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(5));
        token.setAttemptCount(0);

        twoFactorTokenRepository.save(token);

        Map<String, Object> payload = new HashMap<>();
        payload.put("code", code);
        payload.put("firstName", user.getFirstName());
        payload.put("email", user.getEmail());
        n8NService.triggerWorkflow(n8NProperties.getWebhook().getTwoFaCode(), payload);

        return verificationId;
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void completeTwoFactorToggle(Long userId, String verificationId, String code, Boolean enable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!enable) {
            // If disabling, we trust the user is authenticated (Principal check in
            // Controller)
            // But for extra security, we could require password confirmation.
            // For now, simple toggle off.
            user.setTwoFactorEnabled(false);
            userRepository.save(user);
            return;
        }

        TwoFactorToken token = twoFactorTokenRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification session"));

        if (!token.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Invalid session user");
        }

        if (token.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }

        if (token.getAttemptCount() >= 3) {
            twoFactorTokenRepository.delete(token);
            throw new IllegalArgumentException("Too many failed attempts");
        }

        if (!token.getCode().equals(code)) {
            token.setAttemptCount(token.getAttemptCount() + 1);
            twoFactorTokenRepository.save(token);
            throw new IllegalArgumentException("Invalid verification code");
        }

        twoFactorTokenRepository.delete(token);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }
}
