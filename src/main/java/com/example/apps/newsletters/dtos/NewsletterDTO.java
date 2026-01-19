package com.example.apps.newsletters.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterDTO {
    private Long id;
    private String subject;
    private String content;
    private LocalDateTime sentAt;
    private Integer recipientCount;
    private Boolean isDraft;
}
