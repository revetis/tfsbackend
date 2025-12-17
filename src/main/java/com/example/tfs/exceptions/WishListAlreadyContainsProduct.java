package com.example.tfs.exceptions;

public class WishListAlreadyContainsProduct extends RuntimeException {
    public WishListAlreadyContainsProduct(String message) {
        super(message);
    }

}
