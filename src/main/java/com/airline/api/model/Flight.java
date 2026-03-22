package com.airline.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flightNumber;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String airportFrom;
    private String airportTo;
    private int duration;
    private int capacity;
}