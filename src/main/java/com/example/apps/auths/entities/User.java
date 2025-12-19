package com.example.apps.auths.entities;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.apps.auths.enums.Genders;
import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    String refreshToken;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    @Enumerated(EnumType.STRING)
    private Genders gender;
    private Boolean emailVerified = false;
    @Column(name = "accept_terms", nullable = false)
    private Boolean acceptTerms = false;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "birth_of_date")
    private Date birthOfDate;

    @OneToMany(mappedBy = "user", cascade = jakarta.persistence.CascadeType.ALL)
    private List<Address> addresses;

    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    private Boolean enabled = true;

    @OneToOne(mappedBy = "user", cascade = jakarta.persistence.CascadeType.ALL)
    private VerifyEmailToken verifyEmailToken;

    @OneToOne(mappedBy = "user", cascade = jakarta.persistence.CascadeType.ALL)
    private ForgotPasswordToken forgotPasswordToken;

}
