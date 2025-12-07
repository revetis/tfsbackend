package com.example.apps.auth.services.impl;

import com.example.apps.auth.dto.RegisterRequestDTO;
import com.example.apps.auth.dto.RegisterRequestDTOIU;
import com.example.apps.auth.entities.User;
import com.example.apps.auth.repository.IUserRepository;
import com.example.apps.auth.services.IRegisterService;
import com.example.exception.exceptions.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegisterServiceImpl implements IRegisterService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public RegisterRequestDTO register(RegisterRequestDTOIU request){

            User userWithUserName = userRepository.findByUsername(request.getUsername());
            User userWithEmail = userRepository.findByEmail(request.getEmail());
            User userWithPhone = userRepository.findByPhoneNumber(request.getPhoneNumber());

            if (userWithUserName != null){
                throw new ThisUserNameAlreadyRegistered("This username is already registered");
            }
            if (userWithEmail != null){
                throw new ThisEmailAlreadyRegistered("This email address is already registered.");
            }
            if (userWithPhone != null){
                throw new ThisPhoneNumberAlreadyRegistered("This phone number is already registered");
            }
            if (Boolean.FALSE.equals(request.getIsAcceptedTerms())){
                throw new TermsIsNotAccepted("Terms Not Accepted");
            }
            if (!(request.getPassword().equals(request.getPasswordRetry()))){
                throw new PasswordsDoNotMatch("Passwords Do Not Match");
            }

            User newUser = new User();

            BeanUtils.copyProperties(request, newUser);

            newUser.setAccountIsLocked(false);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
//            newUser.setRoles(); !!Bunu sonra ayarla!!
            newUser.setIsEnabled(true);
            userRepository.save(newUser);

            RegisterRequestDTO returnObject = new RegisterRequestDTO();
            BeanUtils.copyProperties(newUser,returnObject);
            return returnObject;
    }
}
