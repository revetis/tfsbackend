package com.example.tfs.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND)
public class WishListAlreadyContainsProduct extends RuntimeException {
    public WishListAlreadyContainsProduct(String message) {
        super(message);
    }

}
