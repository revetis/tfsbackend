package com.example.apps.newsletters.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterDTOIU {
    @NotBlank(message = "Konu boş olamaz")
    private String subject;

    @NotBlank(message = "İçerik boş olamaz")
    private String content;

    @Builder.Default
    private Boolean isDraft = true;
}
