package org.example.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent implements Serializable {
    private String bookingId;
    private String name;
    private String email;
    private String mobile;
    private List<String> seatNumbers;
    private double totalAmount;
    private LocalDateTime timestamp;
}
