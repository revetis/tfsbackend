package com.example.apps.auths.entities;

import java.time.LocalDateTime;

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
@Table(name = "two_factor_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TwoFactorToken extends BaseEntity {

    @Column(nullable = false)
    private String code;

    @Column(name = "verification_id", nullable = false, unique = true)
    private String verificationId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @OneToOne
    private User user;
}
