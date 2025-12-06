package com.example.exception.exceptions;

public class PhoneNumberNotFoundException extends RuntimeException{
    public PhoneNumberNotFoundException(String phoneNumber){
        super(phoneNumber + ": No user with phone number found");
    }
}
