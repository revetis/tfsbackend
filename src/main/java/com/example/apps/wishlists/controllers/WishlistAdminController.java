package com.example.apps.wishlists.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    private com.example.apps.wishlists.repositories.WishlistRepository wishlistRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllWishlists(
            @RequestParam(name = "_start", defaultValue = "0") int start,
            @RequestParam(name = "_end", defaultValue = "10") int end,
            @RequestParam(name = "_sort", defaultValue = "createdAt") String sortField,
            @RequestParam(name = "_order", defaultValue = "DESC") String sortOrder,
            @RequestParam(name = "q", required = false) String search,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "productId", required = false) Long productId) {

        var result = wishlistService.getAllWishlists(start, end, sortField, sortOrder, search, userId, productId);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.totalCount()))
                .header("Access-Control-Expose-Headers", "X-Total-Count")
                .body(ApiTemplate.apiTemplateGenerator(
                        true,
                        HttpStatus.OK.value(),
                        "/rest/api/admin/wishlists/all",
                        null,
                        result.data()));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWishlist(@PathVariable Long id) {
        wishlistRepository.deleteById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.OK.value(),
                "/rest/api/admin/wishlists/" + id,
                null,
                "Wishlist item deleted successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getWishlistByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.OK.value(),
                "/rest/api/admin/wishlists/user/" + userId,
                null,
                wishlistService.getWishlistById(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWishlistById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.OK.value(),
                "/rest/api/admin/wishlists/" + id,
                null,
                wishlistService.getWishlistItemById(id)));
    }
}
