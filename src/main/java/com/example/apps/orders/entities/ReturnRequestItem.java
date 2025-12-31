package com.example.apps.orders.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "return_request_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private int quantity;
}
