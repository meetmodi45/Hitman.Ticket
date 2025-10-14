package org.example.service;

import org.example.dto.AdminBookingDetailDTO;
import org.example.dto.BookingRequest;
import org.example.events.BookingEvent;
import org.example.model.Booking;
import org.example.model.Seat;
import org.example.publisher.BookingEventPublisher;
import org.example.repository.BookingRepository;
import org.example.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final BookingEventPublisher eventPublisher;

    public BookingService(BookingRepository bookingRepository, SeatRepository seatRepository, BookingEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        List<Seat> seats = request.getSeatNumbers().stream()
                .map(seatRepository::findBySeatNumber)
                .collect(Collectors.toList());

        Booking booking = new Booking();
        booking.setName(request.getName());
        booking.setEmail(request.getEmail());
        booking.setMobile(request.getMobile());
        booking.setSeats(seats);

        // ✅ FIX #1: Set the timestamp on the booking object right before you save it.
        booking.setTimestamp(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        for (Seat seat : seats) {
            seat.setBooked(true);
        }
        seatRepository.saveAll(seats);

        // ✅ FIX #2: Create the event using data from the SAVED booking object
        // This ensures the event is consistent with what's in the database.
        BookingEvent event = new BookingEvent(
                savedBooking.getId().toString(),
                savedBooking.getName(),
                savedBooking.getEmail(),
                savedBooking.getMobile(),
                savedBooking.getSeatNumbers(), // Using the helper method from the Booking entity
                request.getSeatNumbers().size() * 500.0,
                savedBooking.getTimestamp() // Using the timestamp that was saved to the DB
        );
        eventPublisher.publishBookingEvent(event);

        return savedBooking;
    }

    public List<AdminBookingDetailDTO> getAllBookedSeatDetails() {
        // This method is now correct, assuming your Booking.java file has been updated
        // with the timestamp field and the getSeatNumbers() helper method.
        List<Booking> allBookings = bookingRepository.findAll();
        List<AdminBookingDetailDTO> bookingDetails = new ArrayList<>();

        for (Booking booking : allBookings) {
            for (Seat seat : booking.getSeats()) {
                AdminBookingDetailDTO detail = new AdminBookingDetailDTO(
                        seat.getSeatNumber(),
                        booking.getId().toString(),
                        booking.getName(),
                        booking.getEmail(),
                        booking.getMobile(),
                        booking.getTimestamp()
                );
                bookingDetails.add(detail);
            }
        }

        return bookingDetails;
    }
}

