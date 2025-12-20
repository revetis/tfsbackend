package com.example.tfs.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND)
public class WishListNotFound extends RuntimeException {
    public WishListNotFound(String message) {
        super(message);
    }

}
