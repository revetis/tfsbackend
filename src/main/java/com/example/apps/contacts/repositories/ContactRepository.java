package com.example.apps.contacts.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.contacts.entities.ContactMessage;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ContactRepository
        extends JpaRepository<ContactMessage, Long>, JpaSpecificationExecutor<ContactMessage> {
}
