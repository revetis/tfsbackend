package com.example.apps.auths.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.auths.entities.Role;

@Repository
public interface IRoleRepository
        extends JpaRepository<Role, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Role> {
    Optional<Role> findByName(String name);
}
