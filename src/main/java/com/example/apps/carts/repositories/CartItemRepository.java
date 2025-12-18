package com.example.apps.carts.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.carts.entities.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
