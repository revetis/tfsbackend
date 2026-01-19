package com.example.apps.invoices.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.apps.invoices.entities.InvoiceSettings;

@Repository
public interface InvoiceSettingsRepository extends JpaRepository<InvoiceSettings, Long> {
    Optional<InvoiceSettings> findByIsActiveTrue();
}
