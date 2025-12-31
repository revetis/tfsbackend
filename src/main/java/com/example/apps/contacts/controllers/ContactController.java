package com.example.apps.contacts.controllers;

import com.example.apps.contacts.dtos.CreateContactMessageRequest;
import com.example.apps.contacts.dtos.ReplyContactMessageRequest;
import com.example.apps.contacts.enums.MessageStatus;
import com.example.apps.contacts.services.ContactService;
import com.example.tfs.maindto.ApiTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest/api")
public class ContactController {
    private final ContactService contactService;

    @PostMapping("/public/contacts")
    public ResponseEntity<?> createMessage(@Valid @RequestBody CreateContactMessageRequest request) {
        contactService.createMessage(request);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/rest/api/public/contacts", null,
                "Message sent successfully"));
    }

    @GetMapping("/admin/contacts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMessages(Pageable pageable) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/rest/api/admin/contacts", null,
                contactService.getMessages(pageable)));
    }

    @GetMapping("/admin/contacts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMessage(@PathVariable Long id) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/rest/api/admin/contacts/" + id, null,
                contactService.getMessage(id)));
    }

    @PutMapping("/admin/contacts/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam MessageStatus status) {
        contactService.updateStatus(id, status);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200,
                "/rest/api/admin/contacts/" + id + "/status", null, "Status updated"));
    }

    @PostMapping("/admin/contacts/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> replyToMessage(@PathVariable Long id,
            @Valid @RequestBody ReplyContactMessageRequest request) {
        contactService.replyToMessage(id, request);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200,
                "/rest/api/admin/contacts/" + id + "/reply", null, "Reply sent successfully"));
    }
}
