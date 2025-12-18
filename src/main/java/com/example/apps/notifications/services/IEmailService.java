package com.example.apps.notifications.services;

public interface IEmailService {
    void send(String to, String subject, String text);
}
