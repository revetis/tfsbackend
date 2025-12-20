package com.example.apps.products.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class ProductVariantException extends RuntimeException {
    public ProductVariantException(String message) {
        super(message);
    }

}
