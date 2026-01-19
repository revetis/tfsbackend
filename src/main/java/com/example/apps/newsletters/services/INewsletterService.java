package com.example.apps.newsletters.services;

import com.example.apps.newsletters.dtos.NewsletterDTO;
import com.example.apps.newsletters.dtos.NewsletterDTOIU;
import com.example.apps.newsletters.dtos.NewsletterSubscriberDTO;
import com.example.apps.newsletters.dtos.SendNewsletterRequest;
import com.example.apps.newsletters.dtos.SubscribeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INewsletterService {
    NewsletterSubscriberDTO subscribe(SubscribeRequest request);

    void unsubscribe(String email);

    Page<NewsletterSubscriberDTO> getAllSubscribers(Pageable pageable, String search);

    NewsletterSubscriberDTO getById(Long id);

    NewsletterSubscriberDTO createSubscriber(SubscribeRequest request);

    void deleteSubscriber(Long id);

    int sendNewsletter(SendNewsletterRequest request);

    // Newsletter CRUD
    NewsletterDTO createNewsletter(NewsletterDTOIU request);

    NewsletterDTO updateNewsletter(Long id, NewsletterDTOIU request);

    void deleteNewsletter(Long id);

    Page<NewsletterDTO> getAllNewsletters(Pageable pageable, String search);

    NewsletterDTO getNewsletterById(Long id);

    int sendNewsletterById(Long id);
}
