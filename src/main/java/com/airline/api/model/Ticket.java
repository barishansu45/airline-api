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

    private String ticketNumber; 
    private String flightNumber; 
    private String passengerName; 
    private String seatNumber;    
    private boolean isCheckedIn;  
}
