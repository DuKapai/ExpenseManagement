package com.example.campusexpensemanager.Expense;

import java.util.Objects;
import java.util.UUID;

public class Expense {
    private String id;
    private String userId;
    private String name;
    private long amount;
    private String dateTime;
    private String category;
    private String notes;

    public Expense(String userId, String name, long amount, String dateTime, String category, String notes) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.dateTime = dateTime;
        this.category = category;
        this.notes = notes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return userId + ";" + name + ";" + amount + ";" + dateTime + ";" + category + ";" + notes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Expense expense = (Expense) obj;
        return id.equals(expense.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Expense fromString(String expenseString) {
        String[] parts = expenseString.split(";");
        return new Expense(parts[0], parts[1], Long.parseLong(parts[2]), parts[3], parts[4], parts[5]);
    }
}
