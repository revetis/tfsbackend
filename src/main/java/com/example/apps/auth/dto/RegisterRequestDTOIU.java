package com.example.apps.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTOIU {

    @NotBlank
    @Size(min = 0, max = 50)
    private String username;

    @NotBlank
    @Size(min = 0, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 0, max = 50)
    private String lastName;

    @NotBlank
    private String password;

    @NotBlank
    private String passwordRetry;

    @NotBlank
    @Size(min = 0, max = 50)
    @Email
    private String email;

    @NotNull
    private Boolean isAcceptedTerms = false;

    @NotNull
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    @NotBlank
    @Size(min = 10, max = 15)
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{10,20}$", message = "Geçerli bir telefon numarası formatı giriniz.")
    private String phoneNumber;

}
