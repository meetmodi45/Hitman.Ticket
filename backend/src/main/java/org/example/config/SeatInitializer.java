package org.example.config;

import org.example.model.Seat;
import org.example.repository.SeatRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeatInitializer implements CommandLineRunner {

    private final SeatRepository seatRepository;

    public SeatInitializer(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if seats are already present
        if(seatRepository.count() == 0) {
            String[] rows = {"A", "B", "C", "D", "E"};
            for(String row : rows) {
                for(int i = 1; i <= 10; i++) {
                    Seat seat = new Seat();
                    seat.setSeatNumber(row + i);
                    seat.setBooked(false);
                    seatRepository.save(seat);
                }
            }
            System.out.println("✅ 50 seats initialized in DB");
        }
    }
}
