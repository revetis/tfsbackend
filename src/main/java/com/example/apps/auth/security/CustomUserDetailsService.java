package com.example.apps.auth.security;

import com.example.apps.auth.entities.Role;
import com.example.apps.auth.entities.User;
import com.example.apps.auth.repository.IUserRepository;
import com.example.exception.exceptions.EmailNotFoundException;
import com.example.exception.exceptions.PhoneNumberNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) throw new UsernameNotFoundException(username);

        return org.springframework.security.core.userdetails.User.builder().username(user.getUsername()).password(user.getPassword()).roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new)).accountLocked(user.getAccountIsLocked()).disabled(!(user.getIsEnabled())).build();
    }

    public UserDetails loadUserByUserInformations(String username, String email, String phoneNumber) {
        User user = userRepository.findUserByInformations(username, email, phoneNumber);

        return org.springframework.security.core.userdetails.User.builder().username(user.getUsername()).password(user.getPassword()).roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new)).accountLocked(user.getAccountIsLocked()).disabled(!(user.getIsEnabled())).build();
    }
}
