package org.example.dto;

import java.time.LocalDateTime;

// Using a record is a modern and concise way to create an immutable DTO.
public record AdminBookingDetailDTO(
        String seatNumber,
        String bookingId,
        String bookedByName,
        String bookedByEmail,
        String bookedByMobile,
        LocalDateTime bookingTimestamp
) {}
