package com.example.apps.auth.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

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

}
