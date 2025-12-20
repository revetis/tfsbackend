package com.example.apps.products.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class ProductVariantColorException extends RuntimeException {
    public ProductVariantColorException(String message) {
        super(message);
    }

}
