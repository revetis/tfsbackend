package com.example.apps.auth.repository;

import com.example.apps.auth.entities.User;
import com.example.exception.exceptions.EmailNotFoundException;
import com.example.exception.exceptions.PhoneNumberNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;


@Repository
public interface IUserRepository extends JpaRepository<User,Long> {
    @Query(value = "SELECT * FROM \\\"users\\\" WHERE username = :username OR email = :email OR phone_number = :phoneNumber",nativeQuery = true)
    User findUserByInformations(String username, String email, String phoneNumber) throws UsernameNotFoundException,EmailNotFoundException,PhoneNumberNotFoundException;

    User findByUsername(String username);
}
