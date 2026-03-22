package com.airline.api.repository;

import com.airline.api.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // PDF gereksinimi: Check-in için bileti numarasından bulmamız gerekebilir
    Ticket findByTicketNumber(String ticketNumber);
    List<Ticket> findByFlightId(Long flightId);
}