package com.example.apps.auth.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank
    @Size(min = 0, max = 50)
    private String username;

    @NotNull
    @NotBlank
    @Size(min = 0, max = 50)
    private String firstName;

    @NotNull
    @NotBlank
    @Size(min = 0, max = 50)
    private String lastName;

    @NotNull
    @NotBlank
    private String password;

    @NotNull
    @NotBlank
    @Size(min = 0, max = 50)
    private String email;

    private Boolean isAcceptedTerms = false;

    @NotNull
    @NotBlank
    private Date dateOfBirth;

    @NotNull
    @NotBlank
    @Size(min = 0, max = 15)
    private String phoneNumber;

    private Boolean isEnabled = true;

    private Boolean accountIsLocked = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = {@JoinColumn(name = "user_id")}, inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private List<Role> roles;
}
