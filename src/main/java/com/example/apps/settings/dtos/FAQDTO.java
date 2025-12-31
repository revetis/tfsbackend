package com.example.apps.settings.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FAQDTO {
    private Long id;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Answer is required")
    private String answer;

    @NotBlank(message = "Category is required")
    private String category;

    private Integer displayOrder;
    private Boolean active;
}
