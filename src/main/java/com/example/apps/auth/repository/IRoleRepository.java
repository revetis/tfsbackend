package com.example.apps.auth.repository;

import com.example.apps.auth.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRoleRepository extends JpaRepository<Role,Long> {
    public Role findByName(String name);
}
