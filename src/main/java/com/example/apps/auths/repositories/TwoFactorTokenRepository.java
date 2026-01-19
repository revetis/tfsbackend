package com.example.apps.auths.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.apps.auths.entities.TwoFactorToken;

public interface TwoFactorTokenRepository extends JpaRepository<TwoFactorToken, Long> {

    Optional<TwoFactorToken> findByVerificationId(String verificationId);

    @Modifying
    @Query("DELETE FROM TwoFactorToken t WHERE t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
