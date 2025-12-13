package com.example.apps.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.apps.auth.entities.Address;
import com.example.apps.auth.entities.User;

public interface IAddressRepository extends JpaRepository<Address, Long> {

    public Address findByUser(User user);

}
