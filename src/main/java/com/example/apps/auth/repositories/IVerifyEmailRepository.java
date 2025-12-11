package com.example.apps.auth.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.auth.entities.VerifyEmailToken;

@Repository
public interface IVerifyEmailRepository extends JpaRepository<VerifyEmailToken, Long> {
    Optional<VerifyEmailToken> findByToken(String token);

}
