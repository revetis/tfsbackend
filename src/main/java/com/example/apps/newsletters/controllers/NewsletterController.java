package com.example.apps.newsletters.controllers;

import com.example.apps.newsletters.dtos.SubscribeRequest;
import com.example.apps.newsletters.dtos.NewsletterSubscriberDTO;
import com.example.apps.newsletters.services.INewsletterService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/api/public/newsletters")
@RequiredArgsConstructor
public class NewsletterController {

    private final INewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<ApiTemplate<Void, NewsletterSubscriberDTO>> subscribe(
            @Valid @RequestBody SubscribeRequest request) {
        NewsletterSubscriberDTO result = newsletterService.subscribe(request);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, "/rest/api/newsletter/subscribe", null, result));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<ApiTemplate<Void, Void>> unsubscribe(@RequestParam String email) {
        newsletterService.unsubscribe(email);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, "/rest/api/newsletter/unsubscribe", null, null));
    }
}
