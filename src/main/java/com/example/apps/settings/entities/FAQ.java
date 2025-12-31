package com.example.apps.settings.entities;

import com.example.tfs.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faqs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FAQ extends BaseEntity {

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "category", nullable = false)
    private String category; // GENERAL, ORDER, PAYMENT, SHIPPING, RETURN

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
