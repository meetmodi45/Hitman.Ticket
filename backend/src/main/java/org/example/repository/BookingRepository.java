package org.example.repository;

import org.example.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Optional: find bookings by email or mobile if needed
    // List<Booking> findByEmail(String email);
}
