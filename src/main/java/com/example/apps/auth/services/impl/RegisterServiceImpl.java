package com.example.apps.auth.services.impl;

import com.example.apps.auth.dto.RegisterRequestDTO;
import com.example.apps.auth.dto.RegisterRequestDTOIU;
import com.example.apps.auth.entities.Role;
import com.example.apps.auth.entities.User;
import com.example.apps.auth.repository.IUserRepository;
import com.example.apps.auth.repository.IRoleRepository;
import com.example.apps.auth.security.CustomUserDetailsService;
import com.example.apps.auth.services.IRegisterService;
import com.example.exception.exceptions.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegisterServiceImpl implements IRegisterService {

    @Autowired
    IUserRepository userRepository;


    @Autowired
    IRoleRepository IRoleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public RegisterRequestDTO register(RegisterRequestDTOIU request) {

        User userRepo = userRepository.findUserByInformations(request.getUsername(), request.getEmail(), request.getPhoneNumber());
        if (userRepo != null) {
            if (userRepo.getUsername() != null) {
                throw new ThisUserNameAlreadyRegistered("This username is already registered");
            }
            if (userRepo.getEmail() != null) {
                throw new ThisEmailAlreadyRegistered("This email address is already registered.");
            }
            if (userRepo.getPhoneNumber() != null) {
                throw new ThisPhoneNumberAlreadyRegistered("This phone number is already registered");
            }
        }
        if (Boolean.FALSE.equals(request.getIsAcceptedTerms())) {
            throw new TermsIsNotAccepted("Terms Not Accepted");
        }
        if (!(request.getPassword().equals(request.getPasswordRetry()))) {
            throw new PasswordsDoNotMatch("Passwords Do Not Match");
        }

        User newUser = new User();
        BeanUtils.copyProperties(request, newUser);
        List<Role> roles = List.of(IRoleRepository.findByName("USER"));

        newUser.setAccountIsLocked(false);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRoles(roles);
        newUser.setIsEnabled(true);
        userRepository.save(newUser);

        RegisterRequestDTO returnObject = new RegisterRequestDTO();
        BeanUtils.copyProperties(newUser, returnObject);
        return returnObject;
    }
}
