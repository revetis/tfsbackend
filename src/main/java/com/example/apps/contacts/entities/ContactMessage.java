package com.example.apps.contacts.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.example.apps.contacts.enums.MessageStatus;
import com.example.tfs.entities.BaseEntity;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
public class ContactMessage extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = true)
    private String phone;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.NEW;

}
