package com.example.apps.wishlists.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.wishlists.services.IWishlistService;
import com.example.tfs.maindto.ApiTemplate;

@RestController
@RequestMapping("/rest/api/admin/wishlists")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class WishlistAdminController {

    @Autowired
    private IWishlistService wishlistService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllWishlists(Pageable pageable) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/admin/wishlists/all",
                null,
                wishlistService.getAllWishlists(pageable)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getWishlistByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpResponseStatus.ACCEPTED.code(),
                "/rest/api/admin/wishlists/user/" + userId,
                null,
                wishlistService.getWishlistById(userId)));
    }
}