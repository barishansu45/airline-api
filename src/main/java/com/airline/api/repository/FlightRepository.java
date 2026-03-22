package com.airline.api.repository;

import com.airline.api.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Page<Flight> findByAirportFromAndAirportToAndDateFromBetweenAndCapacityGreaterThan(
            String airportFrom,
            String airportTo,
            LocalDateTime start,
            LocalDateTime end,
            int capacity,
            Pageable pageable
    );
}

