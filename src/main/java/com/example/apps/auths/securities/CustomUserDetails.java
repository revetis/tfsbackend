package com.example.apps.auths.securities;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.apps.auths.entities.Address;
import com.example.apps.auths.entities.Role;
import com.example.apps.auths.entities.User;
import com.example.apps.auths.enums.Genders;

public class CustomUserDetails implements UserDetails {

    private Long id;
    private String username;
    private String password;
    @SuppressWarnings("unused")
    private String firstName;
    @SuppressWarnings("unused")
    private String lastName;
    private String email;
    @SuppressWarnings("unused")
    private String phoneNumber;
    @SuppressWarnings("unused")
    private String avatarUrl;
    @SuppressWarnings("unused")
    private Boolean emailVerified;
    @SuppressWarnings("unused")
    private LocalDateTime createdAt;
    @SuppressWarnings("unused")
    private LocalDateTime updatedAt;
    private List<Address> addresses;
    private List<Role> roles;
    private Genders gender;
    private Boolean enabled;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.avatarUrl = user.getAvatarUrl();
        this.emailVerified = user.getEmailVerified();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.addresses = user.getAddresses();
        this.roles = user.getRoles();
        this.gender = user.getGender();
        this.enabled = user.getEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null)
            return List.of();
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> role.getName())
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Genders getGender() {
        return gender;
    }
}
