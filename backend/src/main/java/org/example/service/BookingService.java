package org.example.service;

import org.example.dto.BookingRequest;
import org.example.events.BookingEvent; // 👈 Import BookingEvent
import org.example.model.Booking;
import org.example.model.Seat;
import org.example.publisher.BookingEventPublisher; // 👈 Import the publisher
import org.example.repository.BookingRepository;
import org.example.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final BookingEventPublisher eventPublisher; // 👈 Use your publisher

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
        Booking savedBooking = bookingRepository.save(booking);

        for (Seat seat : seats) {
            seat.setBooked(true);
        }
        seatRepository.saveAll(seats);

        // ✅ 4. Create and publish the full event object
        BookingEvent event = new BookingEvent(
                savedBooking.getId().toString(),
                savedBooking.getName(),
                savedBooking.getEmail(),
                savedBooking.getMobile(),
                request.getSeatNumbers(),
                request.getSeatNumbers().size() * 500.0, // Example price calculation
                LocalDateTime.now()
        );
        eventPublisher.publishBookingEvent(event); // 👈 Use the publisher

        return savedBooking;
    }
}