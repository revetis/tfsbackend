package com.example.exception.exceptions;

public class EmailNotFoundException extends RuntimeException{
    public EmailNotFoundException(String email){
        super(email + ": The user with the email address could not be found.");
    }
}
