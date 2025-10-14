package org.example.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.events.BookingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

@Service
public class EmailService {

    // 1. Inject the JavaMailSender
    private final JavaMailSender mailSender;
    private static final String INVOICE_DIR = "invoices/";

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a booking confirmation email with the invoice PDF attached.
     * @param event The event data containing booking details.
     */
    public void sendBookingConfirmationEmail(BookingEvent event) throws MessagingException {
        System.out.println("----------------------------------------");
        System.out.println("📧 Preparing to send email with attachment...");

        // 2. Create a MimeMessage (the "parcel")
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // 3. Use MimeMessageHelper to build the email
        // The 'true' flag enables multipart mode, which is required for attachments.
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(event.getEmail());
        helper.setSubject("Your Booking is Confirmed! - Hitman.Ticket");

        // 4. Create the email body (can be simple text or HTML)
        String emailBody = String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>" +
                        "<p>Hello <b>%s</b>,</p>" +

                        "<p>Thank you for booking your tickets with <b>Hitman.Ticket</b>!</p>" +

                        "<p>Your booking for seat(s) <b>%s</b> has been successfully confirmed.</p>" +

                        "<p>Please find your invoice attached with this email.</p>" +

                        "<p>We look forward to seeing you at the event.</p>" +

                        "<p style='margin-top: 30px;'>Best regards,<br>" +
                        "<b>The Hitman.Ticket Team</b></p>" +
                        "</body>" +
                        "</html>",
                event.getName(),
                String.join(", ", event.getSeatNumbers())
        );

        helper.setText(emailBody, true);

        // 5. Locate the invoice file and attach it
        String invoicePath = Paths.get(INVOICE_DIR, "invoice-" + event.getBookingId() + ".pdf").toString();
        FileSystemResource file = new FileSystemResource(new File(invoicePath));

        if (file.exists()) {
            helper.addAttachment("invoice-" + event.getBookingId() + ".pdf", file);
            System.out.println("📎 Successfully attached invoice: " + file.getFilename());
        } else {
            System.err.println("Could not find invoice file to attach: " + invoicePath);
            // You might want to throw an exception here or handle it gracefully
        }

        // 6. Send the email
        mailSender.send(mimeMessage);

        System.out.println("✅ Email sent successfully to " + event.getEmail());
        System.out.println("----------------------------------------");
    }
}
