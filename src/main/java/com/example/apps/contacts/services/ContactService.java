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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContactService {
    public record ContactPageResult(List<ContactMessage> data, long totalCount) {
    }

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

    public ContactPageResult getAllContactMessages(int page, int size, String sortField, String sortOrder,
            String search, String status) {
        Sort.Direction direction = Sort.Direction.fromString(sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Specification<ContactMessage> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate subjectPredicate = cb.like(cb.lower(root.get("subject")), searchLike);
                Predicate messagePredicate = cb.like(cb.lower(root.get("message")), searchLike);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchLike);
                predicates.add(cb.or(subjectPredicate, messagePredicate, emailPredicate));
            }
            if (StringUtils.hasText(status)) {
                try {
                    MessageStatus messageStatus = MessageStatus.valueOf(status);
                    predicates.add(cb.equal(root.get("status"), messageStatus));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status or handle error
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ContactMessage> messagePage = contactRepository.findAll(spec, pageable);
        return new ContactPageResult(messagePage.getContent(), messagePage.getTotalElements());
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
