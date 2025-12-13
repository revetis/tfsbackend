package com.example.apps.notification.services;

public interface IEmailService {
    void send(String to, String subject, String text);
}
