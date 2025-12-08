package com.example.apps.auth.services.impl;

import com.example.apps.auth.dto.LoginRequestDTO;
import com.example.apps.auth.dto.LoginRequestDTOIU;
import com.example.apps.auth.repository.IUserRepository;
import com.example.apps.auth.security.JWTService;
import com.example.apps.auth.security.PasswordEncoderConfigurations;
import com.example.apps.auth.services.ILoginService;
import com.example.exception.exceptions.UserNotFoundException;
import com.example.exception.exceptions.WrongPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements ILoginService {

    @Autowired
    IUserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoderConfigurations passwordEncoder;

    @Autowired
    JWTService jwtService;

    @Override
    public LoginRequestDTO loginService(LoginRequestDTOIU request) throws Exception {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            LoginRequestDTO returnDTO = new LoginRequestDTO();


            returnDTO.setAccesToken(jwtService.generateToken(userDetails));
            return returnDTO;

        } catch (BadCredentialsException e) {
            throw new WrongPassword("The username or password is incorrect.");
        }catch (UsernameNotFoundException e) {
            throw new UserNotFoundException("Kullanıcı bulunamadı.");
        }catch (Exception e){
            throw new Exception("Bilinmeyen bir giriş hatası oluştu: " + e.getMessage());
        }

    }
}
