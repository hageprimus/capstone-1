package com.pluralsight;

import java.time.*;

public class Transaction {
    private LocalDate date;
    private LocalTime time;
    private String description;
    private String vendor;
    private double amount;

    //Empty constructor
    public Transaction() {
        this.date = LocalDate.EPOCH;
        this.time = LocalTime.MIDNIGHT;
        this.description = "";
        this.vendor = "";
        this.amount = 0;
    }

    //Constructor that accepts literals
    public Transaction(LocalDate date, LocalTime time, String description, String vendor, double amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    //Constructor that accepts strings as each input and parses them automatically
    public Transaction(String date, String time, String description, String vendor, String amount) {
        try {
            this.date = LocalDate.parse(date);
            this.time = LocalTime.parse(time);
            this.description = description;
            this.vendor = vendor;
            this.amount = Double.parseDouble(amount);
        } catch (Exception e) {
            System.out.println("Invalid string passed to com.pluralsight.Transaction constructor.");
            e.printStackTrace();
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isDebit() {
        return amount > 0;
    }

    public String reportInfo() {
        String format = """
                Deposit or debit? %s
                Date: %s
                Time: %s
                Description: %s
                Vendor: %s
                Amount: $%+,.2f
                
                """;
        String debitIndicator = isDebit() ? "Debit" : "Deposit";
        return String.format(format, debitIndicator, date.toString(), time.toString(), description, vendor, amount);
        //TODO: color code output, organize output in table style?
    }

    public String outputToCsv() {
        String format = "%s|%s|%s|%s|%.2f";
        return String.format(format, date.toString(), time.toString(), description, vendor, amount);
    }
}
