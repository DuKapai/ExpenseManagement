package com.example.campusexpensemanager.Database.DAO;

import android.content.Context;
import com.example.campusexpensemanager.Entity.Expense;
import com.example.campusexpensemanager.Database.DatabaseHelper;
import java.util.List;

public class ExpenseDAO {
    private final DatabaseHelper databaseHelper;

    public ExpenseDAO(Context context) {
        this.databaseHelper = new DatabaseHelper(context);
    }

    // Fetch all expenses for a specific user by email
    public List<Expense> getExpensesByEmail(String email) {
        return databaseHelper.getTransactionsByEmail(email); // Implement this in DatabaseHelper
    }

    // Insert a new expense into the database
    public boolean insertExpense(int id, String email, String name, double amount, String description, String type) {
        return databaseHelper.insertTransaction(id, email, name, amount, description, type);
    }

    // Update an existing expense in the database
    public boolean updateExpense(int transactionId, String email, String name, double amount, String description, String type) {
        return databaseHelper.updateTransaction(transactionId, email, name, amount, description, type);
    }

    // Delete an expense from the database
    public boolean deleteExpense(int transactionId) {
        return databaseHelper.deleteTransaction(transactionId);
    }
}

