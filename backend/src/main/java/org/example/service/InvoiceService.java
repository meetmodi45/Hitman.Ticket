package org.example.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.example.events.BookingEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {

    private static final String INVOICE_DIR = "invoices/";

    public void generateInvoice(BookingEvent event) throws IOException {
        new java.io.File(INVOICE_DIR).mkdirs();
        String dest = INVOICE_DIR + "invoice-" + event.getBookingId() + ".pdf";

        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        // --- Define Fonts and Colors ---
        PdfFont mainFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        Color headerColor = new DeviceRgb(0, 0, 139); // Dark Blue
        Color tableHeadColor = new DeviceGray(0.95f);

        // --- 1. Header with Logo and Title ---
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();

        Cell logoCell = new Cell().add(new Paragraph("Hitman.Ticket")
                        .setFont(boldFont).setFontSize(28).setFontColor(headerColor))
                .setBorder(null)
                .setPaddingLeft(10);
        headerTable.addCell(logoCell);

        Cell titleCell = new Cell().add(new Paragraph("BOOKING INVOICE")
                        .setFont(mainFont).setFontSize(14).setFontColor(ColorConstants.GRAY))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(null)
                .setPaddingRight(10);
        headerTable.addCell(titleCell);
        document.add(headerTable);

        document.add(new Paragraph("\n"));
        document.add(new com.itextpdf.layout.element.LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f)));


        // --- 2. Booking and Customer Details ---
        document.add(new Paragraph("Booking Details").setFont(boldFont).setFontSize(16).setMarginTop(20));

        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 2})).useAllAvailableWidth().setMarginTop(10);
        detailsTable.addCell(createDetailCell("Customer:", boldFont));
        detailsTable.addCell(createDetailCell(event.getName(), mainFont));
        detailsTable.addCell(createDetailCell("Email:", boldFont));
        detailsTable.addCell(createDetailCell(event.getEmail(), mainFont));
        detailsTable.addCell(createDetailCell("Booking ID:", boldFont));
        detailsTable.addCell(createDetailCell(event.getBookingId(), mainFont));
        detailsTable.addCell(createDetailCell("Date:", boldFont));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy HH:mm");
        detailsTable.addCell(createDetailCell(event.getTimestamp().format(formatter), mainFont));
        document.add(detailsTable);


        // --- 3. Itemized Seat Summary ---
        document.add(new Paragraph("Order Summary").setFont(boldFont).setFontSize(16).setMarginTop(25));

        Table itemTable = new Table(UnitValue.createPercentArray(new float[]{4, 1})).useAllAvailableWidth().setMarginTop(10);

        itemTable.addHeaderCell(new Cell().add(new Paragraph("Item Description")).setBackgroundColor(tableHeadColor).setBorder(null).setFont(boldFont).setPadding(8));
        itemTable.addHeaderCell(new Cell().add(new Paragraph("Amount (INR)")).setBackgroundColor(tableHeadColor).setBorder(null).setFont(boldFont).setTextAlignment(TextAlignment.RIGHT).setPadding(8));

        double seatPrice = event.getTotalAmount() / event.getSeatNumbers().size();
        for(String seatNumber : event.getSeatNumbers()) {
            itemTable.addCell(createItemCell("Ticket - Seat " + seatNumber, mainFont, TextAlignment.LEFT));
            itemTable.addCell(createItemCell(String.format("%.2f", seatPrice), mainFont, TextAlignment.RIGHT));
        }

        // --- 4. Grand Total ---
        itemTable.addCell(new Cell(1,1).setBorder(null)); // Empty cell for alignment
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{1,1})).useAllAvailableWidth().setMarginTop(10);
        totalTable.addCell(new Cell().add(new Paragraph("Grand Total")).setFont(boldFont).setBorder(null).setTextAlignment(TextAlignment.RIGHT).setPadding(5));
        totalTable.addCell(new Cell().add(new Paragraph(String.format("₹ %.2f", event.getTotalAmount()))).setFont(boldFont).setFontSize(14).setBorder(null).setTextAlignment(TextAlignment.RIGHT).setPadding(5));
        itemTable.addCell(new Cell().add(totalTable).setBorder(null));

        document.add(itemTable);


        // --- 5. Footer ---
        // Corrected the method chain here
        Paragraph footer = new Paragraph("Thank you for choosing Hitman.Ticket. Please show this invoice at the counter.")
                .setFont(mainFont).setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(60);
        document.add(footer);

        document.close();

        System.out.println("🧾 ---> Sleek PDF Invoice generated successfully at: " + dest);
    }

    // Helper methods for clean cell creation
    private Cell createDetailCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font).setMultipliedLeading(1.2f)).setBorder(null).setPadding(4);
    }

    private Cell createItemCell(String text, PdfFont font, TextAlignment alignment) {
        return new Cell().add(new Paragraph(text).setFont(font))
                .setTextAlignment(alignment)
                .setBorder(null)
                .setPadding(8)
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
    }
}

