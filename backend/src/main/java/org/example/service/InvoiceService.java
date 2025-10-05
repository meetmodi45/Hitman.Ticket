package org.example.service;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
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

        // Fonts
        PdfFont helvetica = PdfFontFactory.createFont("Helvetica");
        PdfFont helveticaBold = PdfFontFactory.createFont("Helvetica-Bold");

        // Colors
        Color headerColor = new DeviceRgb(0, 0, 153); // Deep blue
        Color tableHeaderBg = new DeviceGray(0.9f);

        // --- Header Section ---
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        headerTable.setBorder(new SolidBorder(DeviceGray.GRAY, 1));

        Cell logoCell = new Cell()
                .add(new Paragraph("Hitman.Ticket")
                        .setFont(helveticaBold).setFontSize(26).setFontColor(headerColor))
                .setBorder(null)
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(10);
        headerTable.addCell(logoCell);

        Cell titleCell = new Cell()
                .add(new Paragraph("INVOICE\nBooking Confirmation")
                        .setFont(helvetica).setFontSize(12).setFontColor(DeviceGray.GRAY))
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(10);
        headerTable.addCell(titleCell);
        document.add(headerTable);


        // --- Customer & Booking Details Section ---
        document.add(new Paragraph("Booking Details").setFont(helveticaBold).setFontSize(14).setMarginTop(25));

        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2})).useAllAvailableWidth();
        detailsTable.setMarginTop(10);

        detailsTable.addCell(createDetailCell("Customer Name:", helveticaBold));
        detailsTable.addCell(createDetailCell(event.getName(), helvetica));
        detailsTable.addCell(createDetailCell("Booking ID:", helveticaBold));
        detailsTable.addCell(createDetailCell(event.getBookingId(), helvetica));

        detailsTable.addCell(createDetailCell("Email:", helveticaBold));
        detailsTable.addCell(createDetailCell(event.getEmail(), helvetica));
        detailsTable.addCell(createDetailCell("Booking Date:", helveticaBold));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        detailsTable.addCell(createDetailCell(event.getTimestamp().format(formatter), helvetica));

        document.add(detailsTable);


        // --- Itemized Booking Summary ---
        document.add(new Paragraph("Booking Summary").setFont(helveticaBold).setFontSize(14).setMarginTop(25));

        Table itemTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1})).useAllAvailableWidth().setMarginTop(10);

        itemTable.addHeaderCell(createHeaderCell("Item/Seat", helveticaBold, tableHeaderBg));
        itemTable.addHeaderCell(createHeaderCell("Quantity", helveticaBold, tableHeaderBg));
        itemTable.addHeaderCell(createHeaderCell("Price (INR)", helveticaBold, tableHeaderBg));

        double seatPrice = event.getTotalAmount() / event.getSeatNumbers().size();
        for(String seatNumber : event.getSeatNumbers()) {
            itemTable.addCell(createItemCell("Seat " + seatNumber, helvetica, TextAlignment.LEFT));
            itemTable.addCell(createItemCell("1", helvetica, TextAlignment.CENTER));
            itemTable.addCell(createItemCell(String.format("%.2f", seatPrice), helvetica, TextAlignment.RIGHT));
        }

        // --- Total Section ---
        itemTable.addCell(new Cell(1, 2).setBorder(null).add(new Paragraph("Total Amount").setFont(helveticaBold).setTextAlignment(TextAlignment.RIGHT)));
        itemTable.addCell(createTotalCell(String.format("₹ %.2f", event.getTotalAmount()), helveticaBold));

        document.add(itemTable);


        // --- Footer ---
        document.add(new Paragraph("Thank you for booking with Hitman.Ticket!")
                .setFont(helvetica)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(50));

        document.close();

        System.out.println("🧾 ---> PDF Invoice (Improved) generated successfully at: " + dest);
    }

    // Helper methods for creating styled cells
    private Cell createDetailCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font)).setBorder(null).setPadding(4);
    }

    private Cell createHeaderCell(String text, PdfFont font, Color bgColor) {
        return new Cell().add(new Paragraph(text).setFont(font).setFontColor(DeviceGray.BLACK))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(bgColor)
                .setPadding(8);
    }

    private Cell createItemCell(String text, PdfFont font, TextAlignment alignment) {
        return new Cell().add(new Paragraph(text).setFont(font))
                .setTextAlignment(alignment)
                .setPadding(8);
    }

    private Cell createTotalCell(String text, PdfFont font) {
        return new Cell().add(new Paragraph(text).setFont(font))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(new DeviceGray(0.9f))
                .setBold()
                .setPadding(8);
    }
}

