package com.example.apps.auths.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.apps.auths.entities.Address;
import com.example.apps.auths.entities.User;

public interface IAddressRepository extends JpaRepository<Address, Long> {

    public Address findByUser(User user);

}
