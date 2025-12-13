package com.example.apps.auth.dtos;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailTokenDTOIU {

    @Email(message = "Invalid email format")
    String email;

}
