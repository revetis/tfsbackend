package com.example.apps.auth.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.apps.auth.dto.UserLoginDTO;
import com.example.apps.auth.dto.UserLoginDTOIU;
import com.example.apps.auth.dto.UserRegisterDTO;
import com.example.apps.auth.dto.UserRegisterDTOIU;
import com.example.apps.auth.entities.Role;
import com.example.apps.auth.entities.User;
import com.example.apps.auth.repositories.IRoleRepository;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.apps.auth.security.JWTGenerator;
import com.example.apps.auth.services.IUserService;

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

    @Override
    public UserRegisterDTO registerUser(UserRegisterDTOIU request) {

        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if (user.isPresent()) {
            throw new RuntimeException("User already exists");
        }

        Role role = roleRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Role not found"));
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEnabled(true);
        newUser.setRoles(List.of(role));

        userRepository.save(newUser);

        return new UserRegisterDTO(newUser.getUsername());
    }

    @Override
    public UserLoginDTO login(UserLoginDTOIU request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtGenerator.generateToken(user.getUsername(),
                user.getRoles().stream().map(role -> role.getName()).toList());

        return new UserLoginDTO(token);
    }

}
