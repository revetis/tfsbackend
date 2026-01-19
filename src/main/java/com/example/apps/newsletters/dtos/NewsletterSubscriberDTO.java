package com.example.apps.newsletters.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsletterSubscriberDTO {
    private Long id;
    private String email;
    private LocalDateTime subscribedAt;
    private Boolean isActive;
}
