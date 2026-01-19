package com.example.apps.auths.dtos;

import java.util.Date;

import com.example.apps.auths.enums.Genders;

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
public class UserRegisterDTOIU {

    @NotBlank(message = "Kullanıcı adı zorunludur")
    @Size(min = 3, max = 20, message = "Kullanıcı adı 3 ile 20 karakter arasında olmalıdır")
    private String username;

    @NotBlank(message = "Şifre zorunludur")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$", message = "Şifre en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir")
    private String password;

    @NotBlank(message = "Şifre tekrarı zorunludur")
    private String passwordRetry;

    @NotBlank(message = "Ad zorunludur")
    @Size(min = 2, max = 50, message = "Ad 2 ile 50 karakter arasında olmalıdır")
    private String firstName;

    @NotBlank(message = "Soyad zorunludur")
    @Size(min = 2, max = 50, message = "Soyad 2 ile 50 karakter arasında olmalıdır")
    private String lastName;

    @Past(message = "Birth of date must be in the past")
    @NotNull(message = "Birth of date is required")
    private Date birthOfDate;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Accept terms is required")
    private Boolean acceptTerms = false;

    @Size(min = 10, max = 10, message = "Telefon numarası başında 0 olmadan 10 haneli olmalıdır (Örn: 5551234567)")
    @Pattern(regexp = "^[1-9]\\d*$", message = "Telefon numarası geçerli formatta değil")
    private String phoneNumber;

    @NotNull(message = "Gender is required")
    private Genders gender;

    @Pattern(regexp = "^(http|https)://.*$", message = "Avatar URL must be a valid URL")
    private String avatarUrl;

    private Boolean isSubscribedToNewsletter = false;
}
