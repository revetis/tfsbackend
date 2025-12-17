package com.example.tfs.exceptions;

public class TokenBlacklistException extends RuntimeException {
    public TokenBlacklistException(String message) {
        super(message);
    }

}
