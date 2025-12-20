package com.example.apps.wishlists.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND)
public class WishlistException extends RuntimeException {
    public WishlistException(String message) {
        super(message);
    }

}
