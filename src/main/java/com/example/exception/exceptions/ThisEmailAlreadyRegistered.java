package com.example.exception.exceptions;

public class ThisEmailAlreadyRegistered extends RuntimeException {
    public ThisEmailAlreadyRegistered(String message) {
        super(message);
    }
}
