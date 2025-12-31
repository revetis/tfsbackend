package com.example.apps.settings.entities;

import com.example.tfs.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sliders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slider extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "button_text")
    private String buttonText;

    @Column(name = "text_color")
    private String textColor = "#FFFFFF";

    @Column(name = "background_color")
    private String backgroundColor;
}
