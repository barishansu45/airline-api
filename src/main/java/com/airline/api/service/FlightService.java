package com.airline.api.service;

import com.airline.api.model.Flight;
import com.airline.api.model.Ticket;
import com.airline.api.repository.FlightRepository;
import com.airline.api.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private TicketRepository ticketRepository;

    // 1. Tekil Uçuş Kaydetme
    public Flight saveFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    // 2. CSV Dosyası İşleme (Add Flight by File)
    public void processCsvFile(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; } // Header'ı atla
                String[] data = line.split(",");
                Flight f = new Flight();
                f.setFlightNumber(data[0]);
                f.setAirportFrom(data[1]);
                f.setAirportTo(data[2]);
                f.setDateFrom(LocalDateTime.parse(data[3]));
                f.setDateTo(LocalDateTime.parse(data[4]));
                f.setDuration(Integer.parseInt(data[5]));
                f.setCapacity(Integer.parseInt(data[6]));
                flightRepository.save(f);
            }
        }
    }

    // 3. Uçuş Sorgulama (Kapasite > 0 kontrolü ile)
    public Page<Flight> queryFlights(String from, String to, LocalDateTime start, LocalDateTime end, int page) {
        return flightRepository.findByAirportFromAndAirportToAndDateFromBetweenAndCapacityGreaterThan(
                from, to, start, end, 0, PageRequest.of(page, 10));
    }

    // 4. Bilet Alma (Kapasite düşürme dahil)
    @Transactional
    public Ticket buyTicket(Long flightId, String passengerName) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Uçuş bulunamadı!"));

        if (flight.getCapacity() <= 0) {
            throw new RuntimeException("Uçuş dolu! (Sold Out)");
        }

        // Kapasiteyi bir azalt
        flight.setCapacity(flight.getCapacity() - 1);
        flightRepository.save(flight);

        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setPassengerName(passengerName);
        ticket.setTicketNumber("TICK-" + new Random().nextInt(100000));
        return ticketRepository.save(ticket);
    }

    // 5. Check-in (Koltuk Atama)
    public String doCheckIn(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Bilet bulunamadı!"));

        ticket.setSeatNumber("Seat-" + (new Random().nextInt(150) + 1));
        ticketRepository.save(ticket);

        return "Check-in başarılı. Koltuk numaranız: " + ticket.getSeatNumber();
    }

    // 6. Yolcu Listesi
    public List<Ticket> getPassengerList(Long flightId) {
        return ticketRepository.findByFlightId(flightId);
    }
}