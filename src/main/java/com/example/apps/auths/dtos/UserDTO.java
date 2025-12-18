package com.example.apps.auths.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.example.apps.auths.enums.Genders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String username;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;

    private Boolean emailVerified;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<AddressDTO> addresses;

    private List<RoleDTO> roles;

    private Genders gender;

    private Boolean enabled;

}
