package com.example.apps.auth.entities;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.apps.auth.enums.Genders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

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

    private LocalDateTime createdAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user",cascade = jakarta.persistence.CascadeType.ALL)
    private List<Address> addresses;

    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER,cascade = jakarta.persistence.CascadeType.ALL)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    private Boolean enabled = true;

    @OneToOne(mappedBy = "user", cascade = jakarta.persistence.CascadeType.ALL)
    private VerifyEmailToken verifyEmailToken;

    @OneToOne(mappedBy = "user", cascade = jakarta.persistence.CascadeType.ALL)
    private ForgotPasswordToken forgotPasswordToken;

}
