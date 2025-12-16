package com.example.apps.wishlists.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.apps.wishlists.entities.WishList;

public interface WishListRepository extends JpaRepository<WishList, Long> {

}
