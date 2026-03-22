package com.airline.api.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Flight flight;

    private String ticketNumber; // Bilet numarası
    private String flightNumber; // Uçuş numarası
    private String passengerName; // Yolcu ismi
    private String seatNumber;    // Koltuk numarası (Check-in için)
    private boolean isCheckedIn;  // Check-in durumu
}