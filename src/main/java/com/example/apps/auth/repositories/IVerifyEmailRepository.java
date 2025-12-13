package com.example.apps.auth.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.auth.entities.VerifyEmailToken;

@Repository
public interface IVerifyEmailRepository extends JpaRepository<VerifyEmailToken, Long> {
    Optional<VerifyEmailToken> findByToken(String token);

    List<VerifyEmailToken> findAllByUserId(Long id);

    @Transactional
    @Modifying
    @Query("DELETE FROM VerifyEmailToken t WHERE t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

}
