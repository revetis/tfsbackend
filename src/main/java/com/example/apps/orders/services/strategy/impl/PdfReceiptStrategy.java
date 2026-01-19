package com.example.apps.orders.services.strategy.impl;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.services.strategy.ReceiptGeneratorStrategy;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PdfReceiptStrategy implements ReceiptGeneratorStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Fonts
    private static BaseFont BASE_FONT_TR;
    private static Font TITLE_FONT;
    private static Font HEADER_FONT;
    private static Font NORMAL_FONT;
    private static Font SMALL_FONT;

    static {
        try {
            String fontPath = "c:/windows/fonts/arial.ttf";
            BASE_FONT_TR = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            TITLE_FONT = new Font(BASE_FONT_TR, 18, Font.BOLD, Color.DARK_GRAY);
            HEADER_FONT = new Font(BASE_FONT_TR, 12, Font.BOLD, Color.BLACK);
            NORMAL_FONT = new Font(BASE_FONT_TR, 10, Font.NORMAL, Color.BLACK);
            SMALL_FONT = new Font(BASE_FONT_TR, 8, Font.NORMAL, Color.GRAY);
        } catch (Exception e) {
            log.error("Failed to load Arial font, falling back to Helvetica", e);
            TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        }
    }

    @Override
    public byte[] generateReceipt(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // HEADER
            addHeader(document, order);

            // CUSTOMER INFO
            addCustomerInfo(document, order);

            // ITEMS TABLE
            addItemsTable(document, order);

            // TOTALS
            addTotals(document, order);

            // FOOTER
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating receipt for order: {}", order.getId(), e);
            throw new RuntimeException("Sipariş Bilgi Fişi oluşturulurken hata meydan geldi.", e);
        }
    }

    private void addHeader(Document document, Order order) throws Exception {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 60, 40 });

        // Left: Seller Info & Logo
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(0);

        // Brand Name
        Paragraph companyName = new Paragraph("THEFIRSTSTEP", TITLE_FONT);
        leftCell.addElement(companyName);

        // Detailed Seller Info
        if (order.getInvoiceCompanyName() != null) {
            leftCell.addElement(new Paragraph(order.getInvoiceCompanyName(), HEADER_FONT));
            leftCell.addElement(new Paragraph(order.getInvoiceCompanyAddress(), SMALL_FONT));
            leftCell.addElement(new Paragraph("Vergi No: " + order.getInvoiceCompanyTaxNumber(), SMALL_FONT));
            if (order.getInvoiceBankInfo() != null) {
                leftCell.addElement(new Paragraph("Banka: " + order.getInvoiceBankInfo(), SMALL_FONT));
            }
        } else {
            // Fallback for existing orders without snapshot data
            leftCell.addElement(new Paragraph("Satıcı Samet Ture", HEADER_FONT));
            leftCell.addElement(new Paragraph("Mimar Sinan Mah. 178. Sokak No:5/2 Atakum / SAMSUN", SMALL_FONT));
            leftCell.addElement(new Paragraph("Vergi No: 8750714159", SMALL_FONT));
            leftCell.addElement(new Paragraph("Banka: TR14 0086 4011 0000 9618 7004 82", SMALL_FONT));
        }

        leftCell.addElement(new Paragraph("Bu belge resmi fatura değildir.", SMALL_FONT));
        headerTable.addCell(leftCell);

        // Right: Receipt Info
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(0);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph title = new Paragraph("BİLGİ FİŞİ", TITLE_FONT);
        title.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(title);

        Paragraph orderNum = new Paragraph("Sipariş No: " + order.getOrderNumber(), NORMAL_FONT);
        orderNum.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(orderNum);

        Paragraph date = new Paragraph("Tarih: " + LocalDateTime.now().format(DATE_FORMATTER), NORMAL_FONT);
        date.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(date);

        headerTable.addCell(rightCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));
    }

    private void addCustomerInfo(Document document, Order order) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPadding(5);
        cell.setBackgroundColor(new Color(245, 245, 245)); // Light gray background

        cell.addElement(new Paragraph("ALICI BİLGİLERİ", HEADER_FONT));
        cell.addElement(new Paragraph(order.getCustomerName(), NORMAL_FONT));

        if (order.getBillingAddress() != null) {
            cell.addElement(new Paragraph(order.getBillingAddress().getAddressLine(), NORMAL_FONT));
            cell.addElement(new Paragraph(order.getBillingAddress().getDistrictName() + " / "
                    + order.getBillingAddress().getCity() + " / " + order.getBillingAddress().getCountry(),
                    NORMAL_FONT));
            cell.addElement(new Paragraph("Tel: " + order.getBillingAddress().getPhoneNumber(), NORMAL_FONT));
        } else if (order.getShippingAddress() != null) {
            cell.addElement(new Paragraph(order.getShippingAddress().getAddressLine(), NORMAL_FONT));
            cell.addElement(new Paragraph(order.getShippingAddress().getDistrictName() + " / "
                    + order.getShippingAddress().getCity() + " / " + order.getShippingAddress().getCountry(),
                    NORMAL_FONT));
            cell.addElement(new Paragraph("Tel: " + order.getShippingAddress().getPhoneNumber(), NORMAL_FONT));
        }

        if (order.getCustomerEmail() != null) {
            cell.addElement(new Paragraph(order.getCustomerEmail(), NORMAL_FONT));
        }

        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addItemsTable(Document document, Order order) throws Exception {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 40, 20, 20, 20 });

        addTableHeader(table, "ÜRÜN");
        addTableHeader(table, "MİKTAR");
        addTableHeader(table, "BİRİM FİYAT");
        addTableHeader(table, "TOPLAM");

        for (OrderItem item : order.getOrderItems()) {
            // Build product display name: "VariantName (Size)" or just "VariantName" if no
            // size
            String displayName = item.getProductVariantName();
            if (item.getSize() != null) {
                displayName = displayName + " (" + item.getSize().name() + ")";
            }
            if (item.getColor() != null && !item.getColor().isEmpty()) {
                displayName = displayName + " - " + item.getColor();
            }
            addTableCell(table, displayName);
            addTableCell(table, String.valueOf(item.getQuantity()));
            addTableCell(table, formatCurrency(item.getUnitPriceWithTax())); // Using stored Price (Tax Incl)

            BigDecimal lineTotal = item.getUnitPriceWithTax().multiply(BigDecimal.valueOf(item.getQuantity()));
            addTableCell(table, formatCurrency(lineTotal));
        }
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addTotals(Document document, Order order) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        BigDecimal subTotal = order.getSubtotal();
        if (subTotal == null || subTotal.compareTo(BigDecimal.ZERO) == 0) {
            // Fallback: Calculate from items
            subTotal = order.getOrderItems().stream()
                    .map(item -> item.getUnitPriceWithTax().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        addTotalRow(table, "ARA TOPLAM:", formatCurrency(subTotal));

        if (order.getShippingCost() != null && order.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(table, "KARGO:", formatCurrency(order.getShippingCost()));
        }

        if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(table, "İNDİRİM:", "-" + formatCurrency(order.getDiscountAmount()));
        }

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("GENEL TOPLAM:", HEADER_FONT));
        totalLabelCell.setBorder(0);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalLabelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatCurrency(order.getTotalAmount()), HEADER_FONT));
        valueCell.setBorder(0);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addFooter(Document document) throws Exception {
        Paragraph footer = new Paragraph(
                "Siparişiniz için teşekkür ederiz. Faturanız daha sonra e-posta ile gönderilecektir.", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(240, 240, 240));
        cell.setPadding(5);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", NORMAL_FONT));
        cell.setPadding(5);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(0);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(0);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "0,00 TL";
        return String.format("%,.2f TL", amount).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}
