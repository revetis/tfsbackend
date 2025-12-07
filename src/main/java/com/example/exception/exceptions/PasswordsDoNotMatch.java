package com.example.exception.exceptions;

public class PasswordsDoNotMatch extends RuntimeException {
    public PasswordsDoNotMatch(String message) {
        super(message);
    }
}
