package com.example.exception.exceptions;

public class ThisUserNameAlreadyRegistered extends RuntimeException {
    public ThisUserNameAlreadyRegistered(String message) {
        super(message);
    }
}
