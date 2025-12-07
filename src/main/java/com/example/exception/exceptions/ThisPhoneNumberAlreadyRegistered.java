package com.example.exception.exceptions;

public class ThisPhoneNumberAlreadyRegistered extends RuntimeException {
    public ThisPhoneNumberAlreadyRegistered(String message) {
        super(message);
    }
}
