package com.example.apps.wishlists.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.wishlists.entities.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findAllByUserId(Long userId);

    Page<Wishlist> findAllByUserId(Long userId, Pageable pageable);

    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    Page<Wishlist> findAll(Pageable pageable);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}