package org.example.controller;

import org.example.model.Seat;
import org.example.service.SeatService;
import org.example.service.RedisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.dto.BookingRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:3000")
public class SeatController {

    private final SeatService seatService;
    private final RedisService redisService;

    public SeatController(SeatService seatService, RedisService redisService) {
        this.seatService = seatService;
        this.redisService = redisService;
    }

    // Get all seats
    @GetMapping
    public ResponseEntity<List<Seat>> getAllSeats() {
        return ResponseEntity.ok(seatService.getAllSeats());
    }

    // Initialize seats (one-time)
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeSeats() {
        seatService.deleteAllSeats(); // clears existing seats

        String[] rows = {"A", "B", "C", "D", "E"};
        int totalSeats = 0;

        for (String row : rows) {
            for (int i = 1; i <= 10; i++) {
                String seatNumber = row + i;
                Seat seat = new Seat();
                seat.setSeatNumber(seatNumber);
                seat.setBooked(false);
                seatService.saveSeat(seat);
                totalSeats++;
            }
        }
        return ResponseEntity.ok("✅ Initialized " + totalSeats + " seats successfully.");
    }


    // Book seats
    @PostMapping("/book")
    public ResponseEntity<String> bookSeats(@RequestBody BookingRequest bookingRequest) {
        List<String> seatNumbers = bookingRequest.getSeatNumbers();
        List<String> alreadyBooked = new ArrayList<>();
        List<Seat> seatsToBook = new ArrayList<>();

        for (String seatNumber : seatNumbers) {
            Seat seat = seatService.getSeatByNumber(seatNumber);
            if (seat == null) return ResponseEntity.badRequest().body("Seat " + seatNumber + " does not exist.");
            if (seat.isBooked()) alreadyBooked.add(seatNumber);
            else seatsToBook.add(seat);
        }

        if (!alreadyBooked.isEmpty()) {
            return ResponseEntity.badRequest().body("These seats are already booked: " + alreadyBooked);
        }

        for (Seat seat : seatsToBook) {
            seatService.updateSeatStatus(seat.getSeatNumber(), true);
            redisService.unlockSeat(seat.getSeatNumber()); // remove from Redis after booking
        }

        return ResponseEntity.ok("✅ Seats booked successfully: " + seatNumbers);
    }

    // Select a seat (lock in Redis)
    @PostMapping("/select")
    public ResponseEntity<String> selectSeat(@RequestBody Map<String, String> payload) {
        String seatNumber = payload.get("seatNumber"); // must match frontend JSON

        if (seatNumber == null || seatNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Seat number is required.");
        }

        Seat seat = seatService.getSeatByNumber(seatNumber);
        if (seat == null) return ResponseEntity.badRequest().body("Seat does not exist.");
        if (seat.isBooked()) return ResponseEntity.badRequest().body("Seat already booked.");
        if (redisService.isSeatLocked(seatNumber))
            return ResponseEntity.badRequest().body("Seat is currently being booked by another user.");

        // Lock seat for 5 minutes
        boolean locked = redisService.tryLockSeat(seatNumber, Duration.ofMinutes(5));
        if (!locked) return ResponseEntity.badRequest().body("Seat is currently being booked by another user.");

        return ResponseEntity.ok("Seat locked for selection!");
    }
    // SeatController.java
    @PostMapping("/unlock")
    public ResponseEntity<String> unlockSeat(@RequestBody Map<String, String> payload) {
        String seatNumber = payload.get("seatNumber");
        if (seatNumber == null || seatNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Seat number is required.");
        }
        redisService.unlockSeat(seatNumber);
        return ResponseEntity.ok("Seat unlocked successfully!");
    }
    @GetMapping("/locked")
    public ResponseEntity<List<String>> getLockedSeats() {
        List<String> lockedSeats = seatService.getAllSeats().stream()
                .map(Seat::getSeatNumber)
                .filter(redisService::isSeatLocked)
                .toList();
        return ResponseEntity.ok(lockedSeats);
    }

}

