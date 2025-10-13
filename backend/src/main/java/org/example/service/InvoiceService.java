package org.example.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
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
import com.itextpdf.layout.properties.VerticalAlignment;
import org.example.events.BookingEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {

    private static final String INVOICE_DIR = "invoices/";
    private static final double GST_RATE = 0.18; // 18% GST

    public void generateInvoice(BookingEvent event) throws IOException {
        new java.io.File(INVOICE_DIR).mkdirs();
        String dest = INVOICE_DIR + "invoice-" + event.getBookingId() + ".pdf";

        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(25, 30, 25, 30);

        // Modern Color Scheme
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Color primaryBlue = new DeviceRgb(41, 128, 185);      // Modern Blue
        Color accentBlue = new DeviceRgb(52, 152, 219);       // Lighter Blue
        Color blackText = ColorConstants.BLACK;                // Black for all text
        Color lightGray = new DeviceRgb(236, 240, 241);       // Light Background
        Color borderGray = new DeviceRgb(200, 200, 200);      // Border Gray

        // --- 1. Compact Header ---
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{2, 1.5f})).useAllAvailableWidth();
        headerTable.setBorder(Border.NO_BORDER);
        headerTable.setMarginBottom(8);

        // Company Branding
        Cell brandCell = new Cell()
                .add(new Paragraph("Hitman.Ticket")
                        .setFont(boldFont)
                        .setFontSize(26)
                        .setFontColor(primaryBlue)
                        .setMarginBottom(2))
                .add(new Paragraph("Concurrency Control Ticket Booking Platform")
                        .setFont(regularFont)
                        .setFontSize(8)
                        .setFontColor(blackText))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.TOP);
        headerTable.addCell(brandCell);

        // Invoice Badge
        Table invoiceBadge = new Table(1).useAllAvailableWidth();
        invoiceBadge.addCell(new Cell()
                .add(new Paragraph("INVOICE")
                        .setFont(boldFont)
                        .setFontSize(18)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(primaryBlue)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
                .setPadding(8));

        invoiceBadge.addCell(new Cell()
                .add(new Paragraph("INV-" + String.format("%06d", Integer.parseInt(event.getBookingId())))
                        .setFont(boldFont)
                        .setFontSize(9)
                        .setFontColor(blackText))
                .add(new Paragraph(event.getTimestamp().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")))
                        .setFont(regularFont)
                        .setFontSize(8)
                        .setFontColor(blackText)
                        .setMarginTop(2))
                .setBackgroundColor(lightGray)
                .setBorder(Border.NO_BORDER)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER));

        headerTable.addCell(new Cell().add(invoiceBadge).setBorder(Border.NO_BORDER));
        document.add(headerTable);

        // Accent Line
        Table accentLine = new Table(1).useAllAvailableWidth();
        accentLine.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(accentBlue)
                .setHeight(2)
                .setPadding(0));
        document.add(accentLine);
        document.add(new Paragraph("\n").setMarginTop(6));

        // --- 2. Compact Billing Section ---
        Table billingSection = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(10);

        // Bill To
        Table billToTable = new Table(1).useAllAvailableWidth();
        billToTable.addCell(new Cell()
                .add(new Paragraph("BILLED TO")
                        .setFont(boldFont)
                        .setFontSize(9)
                        .setFontColor(primaryBlue)
                        .setMarginBottom(6))
                .setBorder(Border.NO_BORDER));
        billToTable.addCell(createCompactCell(event.getName(), boldFont, 10, blackText));
        billToTable.addCell(createCompactCell(event.getEmail(), regularFont, 8, blackText));
        billToTable.addCell(createCompactCell("Ref: " + event.getBookingId(), regularFont, 8, blackText));

        billingSection.addCell(new Cell()
                .add(billToTable)
                .setBorder(new SolidBorder(borderGray, 0.5f))
                .setBackgroundColor(lightGray)
                .setPadding(10));

        // Service Provider
        Table providerTable = new Table(1).useAllAvailableWidth();
        providerTable.addCell(new Cell()
                .add(new Paragraph("SERVICE PROVIDER")
                        .setFont(boldFont)
                        .setFontSize(9)
                        .setFontColor(primaryBlue)
                        .setMarginBottom(6))
                .setBorder(Border.NO_BORDER));
        providerTable.addCell(createCompactCell("Hitman.Ticket", boldFont, 10, blackText));
        providerTable.addCell(createCompactCell("SVNIT Surat, Gujarat 395007", regularFont, 8, blackText));
        providerTable.addCell(createCompactCell("support@hitman.ticket", regularFont, 8, blackText));

        billingSection.addCell(new Cell()
                .add(providerTable)
                .setBorder(new SolidBorder(borderGray, 0.5f))
                .setPadding(10));

        document.add(billingSection);

        // --- 3. Compact Items Table ---
        document.add(new Paragraph("BOOKING DETAILS")
                .setFont(boldFont)
                .setFontSize(11)
                .setFontColor(primaryBlue)
                .setMarginBottom(6)
                .setMarginTop(2));

        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{0.6f, 3.5f, 0.8f, 1.2f, 1.3f}))
                .useAllAvailableWidth();

        // Headers
        String[] headers = {"#", "DESCRIPTION", "QTY", "RATE ₹", "AMOUNT ₹"};
        for (String header : headers) {
            itemsTable.addHeaderCell(new Cell()
                    .add(new Paragraph(header)
                            .setFont(boldFont)
                            .setFontSize(8))
                    .setBackgroundColor(primaryBlue)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(header.equals("DESCRIPTION") ? TextAlignment.LEFT : TextAlignment.CENTER)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(6));
        }

        // Items
        double seatPrice = event.getTotalAmount() / event.getSeatNumbers().size();
        int itemNumber = 1;
        for (String seatNumber : event.getSeatNumbers()) {
            Color rowColor = (itemNumber % 2 == 0) ? ColorConstants.WHITE : lightGray;
            itemsTable.addCell(createItemCell(String.valueOf(itemNumber++), regularFont, TextAlignment.CENTER, rowColor, blackText));
            itemsTable.addCell(createItemCell("Event Ticket - Seat " + seatNumber, regularFont, TextAlignment.LEFT, rowColor, blackText));
            itemsTable.addCell(createItemCell("1", regularFont, TextAlignment.CENTER, rowColor, blackText));
            itemsTable.addCell(createItemCell(String.format("%.2f", seatPrice), regularFont, TextAlignment.CENTER, rowColor, blackText));
            itemsTable.addCell(createItemCell(String.format("%.2f", seatPrice), boldFont, TextAlignment.CENTER, rowColor, blackText));
        }

        document.add(itemsTable);
        document.add(new Paragraph("\n").setMarginTop(6));

        // --- 4. Compact Summary ---
        Table summaryWrapper = new Table(UnitValue.createPercentArray(new float[]{2f, 1}))
                .useAllAvailableWidth();
        summaryWrapper.addCell(new Cell().setBorder(Border.NO_BORDER));

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1.5f, 1f}))
                .useAllAvailableWidth();

        double subtotal = event.getTotalAmount();
        double gstAmount = subtotal * GST_RATE;
        double grandTotal = subtotal + gstAmount;

        summaryTable.addCell(createSummaryCell("Subtotal", regularFont, 9, blackText));
        summaryTable.addCell(createSummaryCell(String.format("₹ %.2f", subtotal), regularFont, 9, blackText));
        summaryTable.addCell(createSummaryCell("GST 18%", regularFont, 9, blackText));
        summaryTable.addCell(createSummaryCell(String.format("₹ %.2f", gstAmount), regularFont, 9, blackText));

        summaryTable.addCell(new Cell()
                .add(new Paragraph("TOTAL")
                        .setFont(boldFont)
                        .setFontSize(11)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(primaryBlue)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setPadding(8)
                .setPaddingRight(12));

        summaryTable.addCell(new Cell()
                .add(new Paragraph(String.format("₹ %.2f", grandTotal))
                        .setFont(boldFont)
                        .setFontSize(12)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(primaryBlue)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
                .setPadding(8));

        summaryWrapper.addCell(new Cell()
                .add(summaryTable)
                .setBorder(new SolidBorder(borderGray, 0.5f)));

        document.add(summaryWrapper);

        // --- 5. Compact Terms & Footer ---
        document.add(new Paragraph("\n").setMarginTop(10));

        Table termsBox = new Table(1).useAllAvailableWidth();
        termsBox.addCell(new Cell()
                .add(new Paragraph("TERMS & CONDITIONS")
                        .setFont(boldFont)
                        .setFontSize(8)
                        .setFontColor(primaryBlue))
                .setBackgroundColor(lightGray)
                .setBorder(Border.NO_BORDER)
                .setPadding(6)
                .setPaddingBottom(3));

        termsBox.addCell(new Cell()
                .add(new Paragraph(
                        "• Computer-generated invoice, no signature required\n" +
                                "• Tickets are non-refundable and non-transferable\n" +
                                "• Arrive 30 minutes before event start\n" +
                                "• Valid ID required at venue\n" +
                                "• Present this invoice for entry")
                        .setFont(regularFont)
                        .setFontSize(7)
                        .setFontColor(blackText)
                        .setMultipliedLeading(1.3f))
                .setBorder(Border.NO_BORDER)
                .setPadding(6)
                .setPaddingTop(3)
                .setBackgroundColor(lightGray));


        document.add(termsBox);

        // Footer
        document.add(new Paragraph("\n").setMarginTop(8));

        Table footerLine = new Table(1).useAllAvailableWidth();
        footerLine.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(accentBlue)
                .setHeight(1.5f)
                .setPadding(0));
        document.add(footerLine);

        document.add(new Paragraph("Thank You for Choosing Hitman.Ticket")
                .setFont(boldFont)
                .setFontSize(11)
                .setFontColor(primaryBlue)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8));

        document.add(new Paragraph("Questions? support@hitman.ticket")
                .setFont(regularFont)
                .setFontSize(7)
                .setFontColor(blackText)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(3));

        document.close();
        System.out.println("✅ Invoice generated: " + dest);
    }

    // Helper Methods
    private Cell createCompactCell(String text, PdfFont font, int fontSize, Color color) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(fontSize).setFontColor(color))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(3);
    }

    private Cell createItemCell(String text, PdfFont font, TextAlignment alignment, Color bgColor, Color textColor) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(8).setFontColor(textColor))
                .setTextAlignment(alignment)
                .setBackgroundColor(bgColor)
                .setBorder(Border.NO_BORDER)
                .setPadding(6);
    }

    private Cell createSummaryCell(String text, PdfFont font, int fontSize, Color color) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(fontSize).setFontColor(color))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(4)
                .setPaddingBottom(4)
                .setPaddingRight(12);
    }
}
