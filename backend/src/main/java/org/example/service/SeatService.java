package org.example.service;

import org.example.model.Seat;
import org.example.repository.SeatRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public Seat getSeatByNumber(String seatNumber) {
        return seatRepository.findBySeatNumber(seatNumber);
    }

    public void updateSeatStatus(String seatNumber, boolean booked) {
        Seat seat = seatRepository.findBySeatNumber(seatNumber);
        if (seat != null) {
            seat.setBooked(booked);
            seatRepository.save(seat);
        }
    }

    public void saveSeat(Seat seat) {
        seatRepository.save(seat);
    }
    public void deleteAllSeats() {
        seatRepository.deleteAll(); // this clears all seats
    }
    // 🔹 Reset all seats every 3 hours (fixed schedule)
    @Scheduled(cron = "0 0 0/3 * * *")  // runs at 00:00, 03:00, 06:00 … 21:00
    public void resetSeatsEvery3Hours() {
        List<Seat> seats = seatRepository.findAll();
        for (Seat seat : seats) {
            seat.setBooked(false);
            seat.setLockedUntil(null);
        }
        seatRepository.saveAll(seats);
        System.out.println("✅ All seats reset at 3-hour interval");
    }
}
