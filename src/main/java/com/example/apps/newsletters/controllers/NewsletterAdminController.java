package com.example.apps.newsletters.controllers;

import com.example.apps.newsletters.dtos.NewsletterDTO;
import com.example.apps.newsletters.dtos.NewsletterDTOIU;
import com.example.apps.newsletters.dtos.NewsletterSubscriberDTO;
import com.example.apps.newsletters.dtos.SendNewsletterRequest;
import com.example.apps.newsletters.services.INewsletterService;
import com.example.tfs.maindto.ApiTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/api/admin/newsletters")
@RequiredArgsConstructor
public class NewsletterAdminController {

        private final INewsletterService newsletterService;

        @GetMapping
        public ResponseEntity<ApiTemplate<Void, Page<NewsletterDTO>>> getAll(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Page<NewsletterDTO> newsletters = newsletterService.getAllNewsletters(pageable, q);

                return ResponseEntity.ok()
                                .header("X-Total-Count", String.valueOf(newsletters.getTotalElements()))
                                .header("Access-Control-Expose-Headers", "X-Total-Count")
                                .body(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                                                "/rest/api/admin/newsletters", null,
                                                newsletters));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, NewsletterDTO>> getById(@PathVariable Long id) {
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                                "/rest/api/admin/newsletters/" + id, null, newsletterService.getNewsletterById(id)));
        }

        @PostMapping("/create")
        public ResponseEntity<ApiTemplate<Void, NewsletterDTO>> create(@Valid @RequestBody NewsletterDTOIU request) {
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.CREATED.value(),
                                "/rest/api/admin/newsletters/create", null,
                                newsletterService.createNewsletter(request)));
        }

        @PutMapping("/update/{id}")
        public ResponseEntity<ApiTemplate<Void, NewsletterDTO>> update(@PathVariable Long id,
                        @Valid @RequestBody NewsletterDTOIU request) {
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                                "/rest/api/admin/newsletters/update/" + id, null,
                                newsletterService.updateNewsletter(id, request)));
        }

        @DeleteMapping("/delete/{id}")
        public ResponseEntity<ApiTemplate<Void, Void>> delete(@PathVariable Long id) {
                newsletterService.deleteNewsletter(id);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                                "/rest/api/admin/newsletters/delete/" + id, null, null));
        }

        @PostMapping("/send")
        public ResponseEntity<ApiTemplate<Void, Integer>> sendNewsletter(
                        @Valid @RequestBody SendNewsletterRequest request) {
                int sentCount = newsletterService.sendNewsletter(request);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, "/rest/api/admin/newsletters/send",
                                                null, sentCount));
        }

        @PostMapping("/{id}/send")
        public ResponseEntity<ApiTemplate<Void, Integer>> sendNewsletterById(@PathVariable Long id) {
                int sentCount = newsletterService.sendNewsletterById(id);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200,
                                                "/rest/api/admin/newsletters/" + id + "/send", null, sentCount));
        }
}
