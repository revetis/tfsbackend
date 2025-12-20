package com.example.apps.products.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class ProductException extends RuntimeException {
    public ProductException(String message) {
        super(message);
    }

}
