package com.example.apps.auths.dtos;

import java.time.LocalDateTime;
import java.util.Date; // lastLoginDate ve birthOfDate için
import java.util.List;

import com.example.apps.auths.enums.Genders;

import com.example.apps.orders.dtos.OrderDTO;
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

    private Boolean acceptTerms; // Sözleşme onay durumu kritik

    private Genders gender;

    private Date lastLoginDate; // Kullanıcı takibi için önemli

    private Date birthOfDate; // Doğum günü bilgisi

    private Boolean enabled;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<AddressDTO> addresses;

    private List<RoleDTO> roles;

    private List<OrderDTO> orders;

}