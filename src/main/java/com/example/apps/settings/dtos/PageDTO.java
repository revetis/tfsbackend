package com.example.apps.settings.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;

    private String category; // e.g., "shopping", "support", "policy", "contact"

    @NotBlank(message = "Content is required")
    private String content;

    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("showInFooter")
    private Boolean showInFooter;
    private Integer displayOrder;
}
