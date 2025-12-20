package com.example.apps.carts.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class CartException extends RuntimeException {
    public CartException(String message) {
        super(message);
    }

}
