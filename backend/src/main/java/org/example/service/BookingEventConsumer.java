package org.example.service;

import org.example.config.RabbitMQConfig;
import org.example.events.BookingEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class BookingEventConsumer {

    // 1. Inject the InvoiceService to make it available here.
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    // 2. Update the constructor to accept the InvoiceService.
    public BookingEventConsumer(InvoiceService invoiceService, EmailService emailService) {
        this.invoiceService = invoiceService;
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailEvent(BookingEvent event) {
        System.out.println("📬 EMAIL_QUEUE: Received task, handing over to EmailService for booking ID: " + event.getBookingId());
        try {
            // This is the line that was missing!
            emailService.sendBookingConfirmationEmail(event);
        } catch (Exception e) {
            System.err.println("Failed to process email event for booking ID " + event.getBookingId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- NO CHANGES TO THE SMS HANDLER ---
    @RabbitListener(queues = RabbitMQConfig.SMS_QUEUE)
    public void handleSmsEvent(BookingEvent event) {
        System.out.println("📱 SMS SERVICE: Received task for mobile: " + event.getMobile());
        System.out.println("📱 ---> Sending SMS for seats: " + event.getSeatNumbers());
    }


    // --- 3. UPDATED THE INVOICE HANDLER ---
    /**
     * This method now delegates the task of creating a PDF
     * to the specialized InvoiceService.
     */
    @RabbitListener(queues = RabbitMQConfig.INVOICE_QUEUE)
    public void handleInvoiceEvent(BookingEvent event) {
        System.out.println("🧾 INVOICE SERVICE: Received task for booking ID: " + event.getBookingId());
        try {
            // Call the service to do the actual work.
            invoiceService.generateInvoice(event);
        } catch (Exception e) {
            System.err.println("Failed to generate invoice for booking ID: " + event.getBookingId());
            e.printStackTrace();
        }
    }
}