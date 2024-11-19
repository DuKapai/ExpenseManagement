package com.example.campusexpensemanager.Data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanager.Expense.Expense;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ExpenseDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Open the database
    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    // Close the database
    public void close() {
        dbHelper.close();
    }

    // Add an expense
    public void addExpense(Expense expense) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, expense.getUserId());
        values.put(DatabaseHelper.COLUMN_NAME, expense.getName());
        values.put(DatabaseHelper.COLUMN_AMOUNT, expense.getAmount());
        values.put(DatabaseHelper.COLUMN_CATEGORY, expense.getCategory());
        values.put(DatabaseHelper.COLUMN_NOTES, expense.getNotes());
        values.put(DatabaseHelper.COLUMN_DATE_TIME, expense.getDateTime());

        db.insert(DatabaseHelper.TABLE_EXPENSES, null, values);
    }

    // Get all expenses for a specific user
    public List<Expense> getAllExpenses(String userId) {
        List<Expense> expenses = new ArrayList<>();
        Cursor cursor = dbHelper.getUserExpenses(userId); // Using the method from DatabaseHelper

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTES)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE_TIME))
                );
                expenses.add(expense);
            }
            cursor.close();
        }
        return expenses;
    }

    // Update an expense
    public void updateExpense(Expense expense) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, expense.getName());
        values.put(DatabaseHelper.COLUMN_AMOUNT, expense.getAmount());
        values.put(DatabaseHelper.COLUMN_CATEGORY, expense.getCategory());
        values.put(DatabaseHelper.COLUMN_NOTES, expense.getNotes());
        values.put(DatabaseHelper.COLUMN_DATE_TIME, expense.getDateTime());

        db.update(DatabaseHelper.TABLE_EXPENSES, values,
                DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(expense.getUserId())});
    }

    // Delete an expense
    public void deleteExpense(int expenseId) {
        db.delete(DatabaseHelper.TABLE_EXPENSES,
                DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(expenseId)});
    }
}
