package com.example.apps.newsletters.controllers;

import com.example.apps.newsletters.dtos.NewsletterSubscriberDTO;
import com.example.apps.newsletters.services.INewsletterService;
import com.example.tfs.maindto.ApiTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/api/admin/newsletters/subscribers")
@RequiredArgsConstructor
public class NewsletterSubscriberController {

    private final INewsletterService newsletterService;

    @GetMapping
    public ResponseEntity<ApiTemplate<Void, Page<NewsletterSubscriberDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "subscribedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<NewsletterSubscriberDTO> subscribers = newsletterService.getAllSubscribers(pageable, q);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(subscribers.getTotalElements()))
                .header("Access-Control-Expose-Headers", "X-Total-Count")
                .body(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                        "/rest/api/admin/newsletters/subscribers", null,
                        subscribers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, NewsletterSubscriberDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                "/rest/api/admin/newsletters/subscribers/" + id, null, newsletterService.getById(id)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiTemplate<Void, NewsletterSubscriberDTO>> create(
            @org.springframework.web.bind.annotation.RequestBody com.example.apps.newsletters.dtos.SubscribeRequest request) {
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.CREATED.value(),
                "/rest/api/admin/newsletters/subscribers/create", null,
                newsletterService.createSubscriber(request)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiTemplate<Void, Void>> delete(@PathVariable Long id) {
        newsletterService.deleteSubscriber(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                "/rest/api/admin/newsletters/subscribers/delete/" + id, null, null));
    }
}
