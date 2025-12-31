package com.example.apps.settings.entities;

import com.example.tfs.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "category", length = 50)
    private String category; // e.g., "about", "policy", "support", "shopping"

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "show_in_footer", nullable = false)
    private Boolean showInFooter = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}
