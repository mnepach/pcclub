package com.pcclub.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class WorkStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String specifications;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private double pricePerHour;
}