package com.example.apps.carts.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class CartItemException extends RuntimeException {
    public CartItemException(String message) {
        super(message);
    }

}
