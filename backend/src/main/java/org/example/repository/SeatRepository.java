package org.example.repository;

import org.example.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Seat findBySeatNumber(String seatNumber);
}
