package com.securebank.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.securebank.model.Account;
import com.securebank.model.Transaction;
import com.securebank.model.User;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(102, 126, 234);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public byte[] generateAccountStatement(
            Account account,
            User user,
            List<Transaction> transactions,
            int month,
            int year
    ) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add header
            addHeader(document, user, account, month, year);

            // Add account summary
            addAccountSummary(document, account, transactions);

            // Add transactions table
            addTransactionsTable(document, transactions);

            // Add footer
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF statement", e);
        }
    }

    private void addHeader(Document document, User user, Account account, int month, int year) {
        // Bank logo and name
        Paragraph bankName = new Paragraph("SECURE BANKING")
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(bankName);

        Paragraph statementTitle = new Paragraph("Account Statement")
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(statementTitle);

        // Customer and account info
        Table infoTable = new Table(2);
        infoTable.setWidth(UnitValue.createPercentValue(100));

        infoTable.addCell(createInfoCell("Account Holder:", true));
        infoTable.addCell(createInfoCell(user.getFirstName() + " " + user.getLastName(), false));

        infoTable.addCell(createInfoCell("Account Number:", true));
        infoTable.addCell(createInfoCell(account.getAccountNumber(), false));

        infoTable.addCell(createInfoCell("Account Type:", true));
        infoTable.addCell(createInfoCell(account.getAccountType(), false));

        infoTable.addCell(createInfoCell("Statement Period:", true));
        infoTable.addCell(createInfoCell(getMonthName(month) + " " + year, false));

        infoTable.addCell(createInfoCell("Statement Date:", true));
        infoTable.addCell(createInfoCell(LocalDateTime.now().format(DATE_FORMATTER), false));

        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }

    private void addAccountSummary(Document document, Account account, List<Transaction> transactions) {
        Paragraph summaryTitle = new Paragraph("Account Summary")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(summaryTitle);

        BigDecimal totalDeposits = transactions.stream()
                .filter(t -> t.getDestinationAccount().getId().equals(account.getId()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawals = transactions.stream()
                .filter(t -> t.getSourceAccount().getId().equals(account.getId()))
                .map(t -> t.getAmount().add(t.getFee()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Table summaryTable = new Table(2);
        summaryTable.setWidth(UnitValue.createPercentValue(100));

        summaryTable.addCell(createSummaryCell("Current Balance:", false));
        summaryTable.addCell(createSummaryCell("$" + account.getBalance(), true));

        summaryTable.addCell(createSummaryCell("Total Deposits:", false));
        summaryTable.addCell(createSummaryCell("$" + totalDeposits, true));

        summaryTable.addCell(createSummaryCell("Total Withdrawals:", false));
        summaryTable.addCell(createSummaryCell("$" + totalWithdrawals, true));

        summaryTable.addCell(createSummaryCell("Total Transactions:", false));
        summaryTable.addCell(createSummaryCell(String.valueOf(transactions.size()), true));

        document.add(summaryTable);
        document.add(new Paragraph("\n"));
    }

    private void addTransactionsTable(Document document, List<Transaction> transactions) {
        Paragraph transactionsTitle = new Paragraph("Transaction Details")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(transactionsTitle);

        if (transactions.isEmpty()) {
            document.add(new Paragraph("No transactions for this period.").setItalic());
            return;
        }

        float[] columnWidths = {15, 20, 15, 15, 15, 20};
        Table table = new Table(columnWidths);
        table.setWidth(UnitValue.createPercentValue(100));

        // Headers
        table.addHeaderCell(createHeaderCell("Date"));
        table.addHeaderCell(createHeaderCell("Transaction ID"));
        table.addHeaderCell(createHeaderCell("Type"));
        table.addHeaderCell(createHeaderCell("Amount"));
        table.addHeaderCell(createHeaderCell("Fee"));
        table.addHeaderCell(createHeaderCell("Status"));

        // Rows
        for (Transaction txn : transactions) {
            table.addCell(createTableCell(txn.getCreatedAt().format(DATE_FORMATTER)));
            table.addCell(createTableCell(txn.getTransactionId()));
            table.addCell(createTableCell(txn.getType()));
            table.addCell(createTableCell("$" + txn.getAmount()));
            table.addCell(createTableCell("$" + txn.getFee()));
            table.addCell(createTableCell(txn.getStatus()));
        }

        document.add(table);
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph(
                "This is a computer-generated statement and does not require a signature.\n" +
                "For any queries, please contact us at support@securebank.com"
        )
                .setFontSize(8)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
        document.add(footer);
    }

    private Cell createInfoCell(String text, boolean isBold) {
        Cell cell = new Cell().add(new Paragraph(text));
        if (isBold) cell.setBold();
        cell.setBorder(null);
        cell.setPadding(5);
        return cell;
    }

    private Cell createSummaryCell(String text, boolean isValue) {
        Cell cell = new Cell().add(new Paragraph(text));
        if (isValue) {
            cell.setBold();
            cell.setTextAlignment(TextAlignment.RIGHT);
        }
        cell.setPadding(8);
        return cell;
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private Cell createTableCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(6)
                .setFontSize(9);
    }

    private String getMonthName(int month) {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month];
    }
}