package com.example.apps.auth.dtos;

import java.util.Date;

import com.example.apps.auth.enums.Genders;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTOIU {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Past(message = "Birth of date must be in the past")
    @NotNull(message = "Birth of date is required")
    private Date birthOfDate;

    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    @Pattern(regexp = "^\\d+$", message = "Phone number must contain only digits")
    private String phoneNumber;

    @NotNull(message = "Gender is required")
    private Genders gender;
}
