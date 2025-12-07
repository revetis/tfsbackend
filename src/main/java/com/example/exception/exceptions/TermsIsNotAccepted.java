package com.example.exception.exceptions;

public class TermsIsNotAccepted extends RuntimeException {
    public TermsIsNotAccepted(String message) {
        super(message);
    }
}
