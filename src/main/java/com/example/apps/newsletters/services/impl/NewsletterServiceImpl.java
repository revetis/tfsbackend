package com.example.apps.newsletters.services.impl;

import com.example.apps.auths.entities.User;
import com.example.apps.auths.repositories.IUserRepository;
import com.example.apps.newsletters.dtos.*;
import com.example.apps.newsletters.entities.Newsletter;
import com.example.apps.newsletters.entities.NewsletterSubscriber;
import com.example.apps.newsletters.repositories.NewsletterRepository;
import com.example.apps.newsletters.repositories.NewsletterSubscriberRepository;
import com.example.apps.newsletters.services.INewsletterService;
import com.example.apps.notifications.services.IN8NService;
import com.example.apps.notifications.utils.N8NProperties;
import com.example.apps.settings.entities.SiteSettings;
import com.example.apps.settings.repositories.SiteSettingsRepository;
import com.example.tfs.ApplicationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterServiceImpl implements INewsletterService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterRepository newsletterRepository;
    private final SiteSettingsRepository siteSettingsRepository;
    private final IUserRepository userRepository;
    private final IN8NService n8NService;
    private final N8NProperties n8NProperties;
    private final ApplicationProperties applicationProperties;

    @Override
    @Transactional
    public NewsletterSubscriberDTO subscribe(SubscribeRequest request) {
        // Check if already subscribed
        if (subscriberRepository.existsByEmail(request.getEmail())) {
            // Reactivate if inactive, or just return existing
            NewsletterSubscriber existing = subscriberRepository.findByEmail(request.getEmail())
                    .orElseThrow();
            if (!Boolean.TRUE.equals(existing.getIsActive())) {
                existing.setIsActive(true);
                subscriberRepository.save(existing);
                triggerN8NWelcome(existing.getEmail());
            }
            return convertToDTO(existing);
        }

        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(request.getEmail())
                .build();

        NewsletterSubscriber saved = subscriberRepository.save(subscriber);
        log.info("New newsletter subscriber: {}", saved.getEmail());

        // Trigger N8N workflow
        triggerN8NWelcome(saved.getEmail());

        return convertToDTO(saved);
    }

    private void triggerN8NWelcome(String email) {
        try {
            Map<String, Object> payload = buildBasePayload(email);
            payload.put("type", "newsletter_subscription");
            n8NService.triggerWorkflow(
                    n8NProperties.getBaseUrl() + n8NProperties.getWebhook().getNewsletterSubscription(), payload);
        } catch (Exception e) {
            log.error("Failed to trigger N8N newsletter workflow", e);
        }
    }

    /**
     * Build common payload fields for all newsletter emails
     */
    private Map<String, Object> buildBasePayload(String email) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);

        // Get site settings for logo and site name
        SiteSettings settings = siteSettingsRepository.findById(1L).orElse(null);
        if (settings != null) {
            payload.put("siteName", settings.getSiteName());
            payload.put("logoUrl", settings.getLogoUrl());
            payload.put("faviconUrl", settings.getFaviconUrl());
        }

        // Add unsubscribe URL
        String unsubscribeUrl = applicationProperties.getFRONTEND_URL() + "/unsubscribe?email="
                + URLEncoder.encode(email, StandardCharsets.UTF_8);
        payload.put("unsubscribeUrl", unsubscribeUrl);

        return payload;
    }

    @Override
    @Transactional
    public void unsubscribe(String email) {
        subscriberRepository.findByEmail(email).ifPresent(subscriber -> {
            subscriber.setIsActive(false);
            subscriberRepository.save(subscriber);
            log.info("Newsletter unsubscribed: {}", email);
        });
    }

    @Override
    public Page<NewsletterSubscriberDTO> getAllSubscribers(Pageable pageable, String search) {
        Specification<NewsletterSubscriber> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.isBlank()) {
            spec = spec
                    .and((root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%"));
        }

        return subscriberRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public NewsletterSubscriberDTO createSubscriber(SubscribeRequest request) {
        if (subscriberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already subscribed");
        }
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(request.getEmail())
                .isActive(true)
                .subscribedAt(LocalDateTime.now())
                .build();
        return convertToDTO(subscriberRepository.save(subscriber));
    }

    @Override
    @Transactional
    public void deleteSubscriber(Long id) {
        subscriberRepository.deleteById(id);
    }

    @Override
    public NewsletterSubscriberDTO getById(Long id) {
        return subscriberRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
    }

    private NewsletterSubscriberDTO convertToDTO(NewsletterSubscriber subscriber) {
        return NewsletterSubscriberDTO.builder()
                .id(subscriber.getId())
                .email(subscriber.getEmail())
                .subscribedAt(subscriber.getSubscribedAt())
                .isActive(subscriber.getIsActive())
                .build();
    }

    @Override
    @Transactional
    public int sendNewsletter(SendNewsletterRequest request) {
        // Save as a newsletter campaign first
        Newsletter newsletter = Newsletter.builder()
                .subject(request.getSubject())
                .content(request.getContent())
                .isDraft(false)
                .sentAt(LocalDateTime.now())
                .build();

        newsletter = newsletterRepository.save(newsletter);

        int sentCount = performSend(newsletter);

        newsletter.setRecipientCount(sentCount);
        newsletterRepository.save(newsletter);

        return sentCount;
    }

    @Override
    @Transactional
    public NewsletterDTO createNewsletter(NewsletterDTOIU request) {
        Newsletter newsletter = Newsletter.builder()
                .subject(request.getSubject())
                .content(request.getContent())
                .isDraft(request.getIsDraft() != null ? request.getIsDraft() : true)
                .build();

        newsletter = newsletterRepository.save(newsletter);

        if (Boolean.FALSE.equals(newsletter.getIsDraft())) {
            int sentCount = performSend(newsletter);
            newsletter.setSentAt(LocalDateTime.now());
            newsletter.setRecipientCount(sentCount);
            newsletter = newsletterRepository.save(newsletter);
        }

        return convertNewsletterToDTO(newsletter);
    }

    @Override
    @Transactional
    public NewsletterDTO updateNewsletter(Long id, NewsletterDTOIU request) {
        Newsletter newsletter = newsletterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Newsletter not found"));

        newsletter.setSubject(request.getSubject());
        newsletter.setContent(request.getContent());
        newsletter.setIsDraft(request.getIsDraft());

        return convertNewsletterToDTO(newsletterRepository.save(newsletter));
    }

    @Override
    @Transactional
    public void deleteNewsletter(Long id) {
        newsletterRepository.deleteById(id);
    }

    @Override
    public Page<NewsletterDTO> getAllNewsletters(Pageable pageable, String search) {
        Specification<Newsletter> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("subject")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("content")), "%" + search.toLowerCase() + "%")));
        }

        return newsletterRepository.findAll(spec, pageable).map(this::convertNewsletterToDTO);
    }

    @Override
    public NewsletterDTO getNewsletterById(Long id) {
        return newsletterRepository.findById(id)
                .map(this::convertNewsletterToDTO)
                .orElseThrow(() -> new RuntimeException("Newsletter not found"));
    }

    @Override
    @Transactional
    public int sendNewsletterById(Long id) {
        Newsletter newsletter = newsletterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Newsletter not found"));

        int sentCount = performSend(newsletter);

        newsletter.setIsDraft(false);
        newsletter.setSentAt(LocalDateTime.now());
        newsletter.setRecipientCount(sentCount);
        newsletterRepository.save(newsletter);

        return sentCount;
    }

    private int performSend(Newsletter newsletter) {
        // Get all active anonymous subscribers
        Set<String> recipientEmails = subscriberRepository.findAll()
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .map(NewsletterSubscriber::getEmail)
                .collect(Collectors.toSet());

        // Add subscribed registered users
        userRepository.findByIsSubscribedToNewsletterTrue()
                .stream()
                .filter(u -> Boolean.TRUE.equals(u.getEnabled()))
                .map(User::getEmail)
                .forEach(recipientEmails::add);

        int sentCount = 0;
        for (String email : recipientEmails) {
            try {
                Map<String, Object> payload = buildBasePayload(email);
                payload.put("subject", newsletter.getSubject());
                payload.put("content", newsletter.getContent());
                payload.put("type", "newsletter_campaign");
                n8NService.triggerWorkflow(
                        n8NProperties.getBaseUrl() + n8NProperties.getWebhook().getNewsletterCampaign(), payload);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send newsletter to: {}", email, e);
            }
        }

        log.info("Newsletter sent to {} recipients", sentCount);
        return sentCount;
    }

    private NewsletterDTO convertNewsletterToDTO(Newsletter newsletter) {
        return NewsletterDTO.builder()
                .id(newsletter.getId())
                .subject(newsletter.getSubject())
                .content(newsletter.getContent())
                .sentAt(newsletter.getSentAt())
                .recipientCount(newsletter.getRecipientCount())
                .isDraft(newsletter.getIsDraft())
                .build();
    }
}
