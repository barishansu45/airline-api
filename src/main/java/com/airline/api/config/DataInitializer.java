package com.airline.api.config;

import com.airline.api.model.Flight;
import com.airline.api.model.Ticket;
import com.airline.api.repository.FlightRepository;
import com.airline.api.repository.TicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(FlightRepository flightRepository, TicketRepository ticketRepository) {
        return args -> {
            System.out.println("Initializing sample flight data...");

            // Flight 1: Istanbul to Antalya
            Flight f1 = new Flight();
            f1.setFlightNumber("TK1923");
            f1.setAirportFrom("IST");
            f1.setAirportTo("AYT");
            f1.setDateFrom(LocalDateTime.of(2026, 5, 10, 10, 0));
            f1.setDateTo(LocalDateTime.of(2026, 5, 10, 12, 0));
            f1.setDuration(120);
            f1.setCapacity(150);
            flightRepository.save(f1);

            // Flight 2: Istanbul to Ankara
            Flight f2 = new Flight();
            f2.setFlightNumber("TK2112");
            f2.setAirportFrom("IST");
            f2.setAirportTo("ESB");
            f2.setDateFrom(LocalDateTime.of(2026, 5, 10, 07, 30));
            f2.setDateTo(LocalDateTime.of(2026, 5, 10, 8, 45));
            f2.setDuration(75);
            f2.setCapacity(120);
            flightRepository.save(f2);

            // Flight 3: Izmir to Istanbul
            Flight f3 = new Flight();
            f3.setFlightNumber("PC2201");
            f3.setAirportFrom("ADB");
            f3.setAirportTo("IST");
            f3.setDateFrom(LocalDateTime.of(2026, 5, 11, 14, 0));
            f3.setDateTo(LocalDateTime.of(2026, 5, 11, 15, 10));
            f3.setDuration(70);
            f3.setCapacity(180);
            flightRepository.save(f3);

            // Sample Ticket for the first flight
            Ticket t1 = new Ticket();
            t1.setFlight(f1);
            t1.setPassengerName("Baris Hansu");
            t1.setTicketNumber("TICK-88888");
            t1.setSeatNumber("12F");
            ticketRepository.save(t1);

            System.out.println("Sample data initialized successfully!");
        };
    }
}
