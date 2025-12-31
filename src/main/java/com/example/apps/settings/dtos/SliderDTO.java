package com.example.apps.settings.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SliderDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String linkUrl;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String buttonText;
    private String textColor;
    private String backgroundColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
