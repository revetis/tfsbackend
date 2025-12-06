package com.example.apps.auth.repository;

import com.example.apps.auth.entities.User;
import com.example.exception.exceptions.EmailNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;


@Repository
public interface IUserRepository extends JpaRepository<User,Long> {

    User findByUsername(String username) throws UsernameNotFoundException;

    User findByEmail(String email) throws EmailNotFoundException;
}
