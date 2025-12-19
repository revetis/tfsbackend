package com.example.apps.auths.entities;

import java.util.Date;

import com.example.tfs.entities.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "forgot_password_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordToken extends BaseEntity {

    @Column(unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = jakarta.persistence.FetchType.EAGER)
    private User user;

    private Date expiresAt;

}
