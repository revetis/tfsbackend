package com.example.apps.products.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.services.IProductService;
import com.example.tfs.maindto.ApiErrorTemplate;
import com.example.tfs.maindto.ApiTemplate;

@RestController
@RequestMapping("/rest/api/public/products")
public class ProductPublicController {

    @Autowired
    private IProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        ProductDTO response = productService.getProductById(id);

        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiErrorTemplate.apiErrorTemplateGenerator(
                            false,
                            HttpStatus.NOT_FOUND.value(),
                            "/rest/api/public/products/" + id,
                            "Product not found"));
        }

        return ResponseEntity.ok(
                ApiTemplate.apiTemplateGenerator(
                        true,
                        HttpStatus.OK.value(),
                        "/rest/api/public/products/" + id,
                        null,
                        response));
    }
}
