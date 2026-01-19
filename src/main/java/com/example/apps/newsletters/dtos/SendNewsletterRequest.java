package com.example.apps.newsletters.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendNewsletterRequest {
    @NotBlank(message = "Konu boş olamaz")
    private String subject;

    @NotBlank(message = "İçerik boş olamaz")
    private String content;
}
