package com.example.apps.newsletters.repositories;

import com.example.apps.newsletters.entities.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository
        extends JpaRepository<NewsletterSubscriber, Long>, JpaSpecificationExecutor<NewsletterSubscriber> {
    Optional<NewsletterSubscriber> findByEmail(String email);

    boolean existsByEmail(String email);
}
