package com.airline.api.controller;

import com.airline.api.model.Flight;
import com.airline.api.model.Ticket;
import com.airline.api.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    // 1. Tekil Uçuş Ekleme (Add Flight)
    @Operation(summary = "Adds a flight to airline schedule")
    @PostMapping("/add")
    public ResponseEntity<Flight> addFlight(@RequestBody Flight flight) {
        return ResponseEntity.ok(flightService.saveFlight(flight));
    }

    // 2. CSV ile Toplu Uçuş Ekleme (Add Flight by File)
    @Operation(summary = "Adds all the flights in the .csv file")
    @PostMapping(value = "/add-by-file", consumes = "multipart/form-data")
    public ResponseEntity<String> addFlightByFile(@RequestParam("file") MultipartFile file) {
        try {
            flightService.processCsvFile(file);
            return ResponseEntity.ok("Dosya başarıyla işlendi ve uçuşlar eklendi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Dosya işleme hatası: " + e.getMessage());
        }
    }

    // 3. Uçuş Sorgulama (Query Flight - Paging Dahil)
    @Operation(summary = "Query available flights (No seats = hidden)")
    @GetMapping("/query")
    public ResponseEntity<Page<Flight>> queryFlights(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String dateFrom,
            @RequestParam(defaultValue = "0") int page) {

        LocalDateTime start = LocalDateTime.parse(dateFrom);
        LocalDateTime end = start.plusDays(1);

        return ResponseEntity.ok(flightService.queryFlights(from, to, start, end, page));
    }

    // 4. Bilet Alma (Buy Ticket)
    @Operation(summary = "Buy a ticket for a flight")
    @PostMapping("/buy-ticket")
    public ResponseEntity<Ticket> buyTicket(@RequestParam Long flightId, @RequestParam String passengerName) {
        return ResponseEntity.ok(flightService.buyTicket(flightId, passengerName));
    }

    // 5. Check-in Yapma (Check in)
    @Operation(summary = "Assign seat to Passenger on flight")
    @PostMapping("/check-in/{ticketId}")
    public ResponseEntity<String> doCheckIn(@PathVariable Long ticketId) {
        return ResponseEntity.ok(flightService.doCheckIn(ticketId));
    }

    // 6. Yolcu Listesi (Query Flight Passenger List)
    @Operation(summary = "List of passengers of a flight")
    @GetMapping("/{flightId}/passengers")
    public ResponseEntity<List<Ticket>> getPassengerList(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightService.getPassengerList(flightId));
    }
}