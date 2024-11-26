package com.example.campusexpensemanager.Entity;

import android.util.Log;
import java.util.Objects;

public class Expense {
    private int id;
    private String email;
    private String name;
    private double amount;
    private String dateTime;
    private String description;
    private String type;

    // Constructor with updated properties
    public Expense(int id, double amount, String email, String name, String dateTime, String description, String type) {
        this.id = id;
        this.amount = amount;
        this.email = email;
        this.name = name;
        this.dateTime = dateTime;
        this.description = description;
        this.type = type;
    }

    // Getters and setters for all fields
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return email + ";" + name + ";" + amount + ";" + dateTime + ";" + ";" + description + ";" + type;
    }

    public static Expense fromString(String expenseString) {
        try {
            String[] parts = expenseString.split(";");
            if (parts.length != 6) { // Adjusted for 6 parts
                Log.e("Expense", "Invalid expense string format: " + expenseString);
                return null;
            }
            // Using parts[3] directly for dateTime as String
            //return new Expense(parts[0], parts[1], Long.parseLong(parts[2]), parts[3], parts[4], parts[5]);
        } catch (NumberFormatException e) {
            Log.e("Expense", "Error parsing expense string: " + expenseString, e);
            return null;
        }
        return null;
    }
}
