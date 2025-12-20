package com.example.tfs.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.FORBIDDEN)
public class AddressDenied extends RuntimeException {
    public AddressDenied(String message) {
        super(message);
    }

}
