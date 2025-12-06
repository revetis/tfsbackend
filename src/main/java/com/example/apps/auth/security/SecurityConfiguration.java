package com.example.apps.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfiguration {

    @Autowired
    PasswordEncoderConfigurations passwordEncoder;


    //Filtrede butun http requestleri icin auth gerekli olarak ayarliyor
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity.csrf(csrf -> csrf.disable()).authorizeHttpRequests(request -> request.requestMatchers("/api/rest/public/**").permitAll().requestMatchers("/api/rest/secure/**").hasRole("ADMIN").anyRequest().denyAll()).httpBasic(Customizer.withDefaults()).build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity,PasswordEncoderConfigurations passwordEncoder,CustomUserDetailsService userDetailsService ){
        AuthenticationManagerBuilder authBuilder =
                httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder.passwordEncoder());

        return authBuilder.build();
    }

}
