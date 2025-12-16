package com.example.settings.exceptions;

public class WishListAlreadyContainsProduct extends RuntimeException {
    public WishListAlreadyContainsProduct(String message) {
        super(message);
    }

}
