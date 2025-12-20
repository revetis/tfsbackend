package com.example.apps.products.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class SubCategoryException extends RuntimeException {
    public SubCategoryException(String message) {
        super(message);
    }

}
