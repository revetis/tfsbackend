package com.example.apps.contacts.services;

import com.example.apps.contacts.dtos.CreateContactMessageRequest;
import com.example.apps.contacts.dtos.ReplyContactMessageRequest;
import com.example.apps.contacts.entities.ContactMessage;
import com.example.apps.contacts.enums.MessageStatus;
import com.example.apps.contacts.repositories.ContactRepository;
import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final IN8NService n8nService;
    private final N8NProperties n8NProperties;

    public void createMessage(CreateContactMessageRequest request) {
        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setName(request.getName());
        contactMessage.setEmail(request.getEmail());
        contactMessage.setPhone(request.getPhone());
        contactMessage.setSubject(request.getSubject());
        contactMessage.setMessage(request.getMessage());
        contactRepository.save(contactMessage);
    }

    public Page<ContactMessage> getMessages(Pageable pageable) {
        return contactRepository.findAll(pageable);
    }

    public ContactMessage getMessage(Long id) {
        return contactRepository.findById(id).orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public void updateStatus(Long id, MessageStatus status) {
        ContactMessage message = getMessage(id);
        message.setStatus(status);
        contactRepository.save(message);
    }

    public void replyToMessage(Long id, ReplyContactMessageRequest request) {
        ContactMessage message = getMessage(id);

        // Prepare payload for n8n
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", message.getId());
        payload.put("name", message.getName());
        payload.put("email", message.getEmail());
        payload.put("subject", message.getSubject());
        payload.put("originalMessage", message.getMessage());
        payload.put("replyMessage", request.getReplyMessage());
        payload.put("repliedAt", LocalDateTime.now().toString());

        // Call n8n webhook
        try {
            n8nService.triggerWorkflow(n8NProperties.getWebhook().getContactReply(), payload);
        } catch (Exception e) {
            // Log error but proceed to update status if acceptable, or throw
            e.printStackTrace();
            throw new RuntimeException("Failed to trigger n8n webhook: " + e.getMessage());
        }

        // Update status
        message.setStatus(MessageStatus.REPLIED);
        contactRepository.save(message);
    }
}
