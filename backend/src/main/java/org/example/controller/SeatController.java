package org.example.controller;

import org.example.dto.BookingRequest;
import org.example.model.Booking;
import org.example.model.Seat;
import org.example.repository.BookingRepository;
import org.example.service.BookingService;
import org.example.service.RedisService;
import org.example.service.SeatService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.dto.AdminBookingDetailDTO;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:3000")
public class SeatController {

    private final SeatService seatService;
    private final RedisService redisService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public SeatController(SeatService seatService, RedisService redisService, BookingService bookingService, BookingRepository bookingRepository) {
        this.seatService = seatService;
        this.redisService = redisService;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeSeats() {
        // Delete dependent records first
        bookingRepository.deleteAll();
        // Then delete the seats
        seatService.deleteAllSeats();

        String[] rows = {"A", "B", "C", "D", "E"};
        for (String row : rows) {
            for (int i = 1; i <= 10; i++) {
                Seat seat = new Seat();
                seat.setSeatNumber(row + i);
                seat.setBooked(false);
                seatService.saveSeat(seat);
            }
        }
        return ResponseEntity.ok("✅ Initialized 50 seats successfully.");
    }

    @GetMapping
    public ResponseEntity<List<Seat>> getAllSeats() {
        return ResponseEntity.ok(seatService.getAllSeats());
    }

    @PostMapping("/select")
    public ResponseEntity<String> selectSeat(@RequestBody Map<String, String> payload) {
        String seatNumber = payload.get("seatNumber");
        if (seatNumber == null || seatNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Seat number is required.");
        }
        Seat seat = seatService.getSeatByNumber(seatNumber);
        if (seat == null) return ResponseEntity.badRequest().body("Seat does not exist.");
        if (seat.isBooked()) return ResponseEntity.badRequest().body("Seat already booked.");
        if (redisService.isSeatLocked(seatNumber))
            return ResponseEntity.badRequest().body("Seat is currently held by another user.");

        boolean locked = redisService.tryLockSeat(seatNumber, Duration.ofMinutes(5));
        if (!locked) return ResponseEntity.badRequest().body("Seat was just taken. Please try another.");

        return ResponseEntity.ok("Seat locked for 5 minutes!");
    }

    @PostMapping("/unlock")
    public ResponseEntity<String> unlockSeat(@RequestBody Map<String, String> payload) {
        String seatNumber = payload.get("seatNumber");
        if (seatNumber == null || seatNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Seat number is required.");
        }
        redisService.unlockSeat(seatNumber);
        return ResponseEntity.ok("Seat unlocked.");
    }

    @GetMapping("/locked")
    public ResponseEntity<List<String>> getLockedSeats() {
        return ResponseEntity.ok(seatService.getAllSeats().stream()
                .map(Seat::getSeatNumber)
                .filter(redisService::isSeatLocked)
                .toList());
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookSeats(@RequestBody BookingRequest bookingRequest) {
        try {
            Booking savedBooking = bookingService.createBooking(bookingRequest);

            for (String seatNumber : bookingRequest.getSeatNumbers()) {
                redisService.unlockSeat(seatNumber);
            }

            Map<String, String> response = Map.of(
                    "bookingId", savedBooking.getId().toString(),
                    "message", "✅ Booking confirmed for " + bookingRequest.getName() + "!"
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Booking failed: " + e.getMessage()));
        }
    }
    @GetMapping("/admin/booked-details")
    public ResponseEntity<List<AdminBookingDetailDTO>> getBookedSeatDetails() {
        List<AdminBookingDetailDTO> details = bookingService.getAllBookedSeatDetails();
        return ResponseEntity.ok(details);
    }

    @GetMapping("/invoice/{bookingId}")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable String bookingId) {
        try {
            Path fileStorageLocation = Paths.get("invoices").toAbsolutePath().normalize();
            Path filePath = fileStorageLocation.resolve("invoice-" + bookingId + ".pdf").normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + bookingId + ".pdf\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}