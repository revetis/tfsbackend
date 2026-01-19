package com.example.apps.carts.controllers;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.carts.services.ICartService;
import com.example.tfs.maindto.ApiTemplate;

@RestController
@RequestMapping("/rest/api/admin/carts")
@Validated
public class CartAdminController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllCarts(
            @RequestParam(name = "_start", defaultValue = "0") int start,
            @RequestParam(name = "_end", defaultValue = "10") int end,
            @RequestParam(name = "_sort", defaultValue = "createdAt") String sortField,
            @RequestParam(name = "_order", defaultValue = "DESC") String sortOrder,
            @RequestParam(name = "q", required = false) String search,
            @RequestParam(name = "userId", required = false) Long userId) {

        var result = cartService.getAllCarts(start, end, sortField, sortOrder, search, userId);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.totalCount()))
                .header("Access-Control-Expose-Headers", "X-Total-Count")
                .body(ApiTemplate.apiTemplateGenerator(true, HttpStatus.SC_OK,
                        "/rest/api/admin/carts/all", null, result.data()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.SC_OK,
                "/rest/api/admin/carts/" + id, null, cartService.getCartById(id)));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCart(@PathVariable Long id) {
        cartService.deleteCartById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.SC_OK,
                "/rest/api/admin/carts/" + id, null, "Cart deleted successfully"));
    }
}
