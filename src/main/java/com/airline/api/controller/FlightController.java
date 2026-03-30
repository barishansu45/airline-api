package com.airline.api.controller;

import com.airline.api.model.Flight;
import com.airline.api.model.Ticket;
import com.airline.api.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat; 
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


    @Operation(summary = "Adds a flight to airline schedule")
    @PostMapping("/add")
    public ResponseEntity<Flight> addFlight(@RequestBody Flight flight) {
        return ResponseEntity.ok(flightService.saveFlight(flight));
    }

    @Operation(summary = "Adds all the flights in the .csv file")
    @PostMapping(value = "/add-by-file", consumes = "multipart/form-data")
    public ResponseEntity<String> addFlightByFile(@RequestParam("file") MultipartFile file) {
        try {
            flightService.processCsvFile(file);
            return ResponseEntity.ok("The file has been successfully processed and flights have been added.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File processing error: " + e.getMessage());
        }
    }

    @Operation(summary = "Query available flights (No seats = hidden)")
    @GetMapping("/query")
    public ResponseEntity<Page<Flight>> queryFlights(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime dateFrom,
            @RequestParam(defaultValue = "0") int page) {

        LocalDateTime end = dateFrom.plusDays(1);

        return ResponseEntity.ok(flightService.queryFlights(from, to, dateFrom, end, page));
    }

    @Operation(summary = "Buy a ticket for a flight")
    @PostMapping("/buy-ticket")
    public ResponseEntity<Ticket> buyTicket(@RequestParam Long flightId, @RequestParam String passengerName) {
        return ResponseEntity.ok(flightService.buyTicket(flightId, passengerName));
    }

    @Operation(summary = "Assign seat to Passenger on flight")
    @PostMapping("/check-in/{ticketId}")
    public ResponseEntity<String> doCheckIn(@PathVariable Long ticketId) {
        return ResponseEntity.ok(flightService.doCheckIn(ticketId));
    }

    @Operation(summary = "List of passengers of a flight")
    @GetMapping("/{flightId}/passengers")
    public ResponseEntity<List<Ticket>> getPassengerList(@PathVariable Long flightId) {
        return ResponseEntity.ok(flightService.getPassengerList(flightId));
    }
}
