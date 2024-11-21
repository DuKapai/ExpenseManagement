package com.example.campusexpensemanager.Data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.campusexpensemanager.Expense.Expense;
import com.example.campusexpensemanager.Notification.NotificationRecord;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "campus_expenses.db";
    private static final int DATABASE_VERSION = 2;

    // Table names
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String TABLE_USERS = "users";

    // Column names for expenses
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_DATE_TIME = "date_time";

    // Column names for notifications
    public static final String COLUMN_ACTION_TYPE = "action_type";
    public static final String COLUMN_EXPENSE_ID = "expense_id";
    public static final String COLUMN_DESCRIPTION = "description";

    // Column names for users
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    private SQLiteDatabase db;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL("CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_AMOUNT + " INTEGER, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_NOTES + " TEXT, " +
                COLUMN_DATE_TIME + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_ACTION_TYPE + " TEXT, " +
                COLUMN_EXPENSE_ID + " INTEGER, " +
                COLUMN_DATE_TIME + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Open database
    public void open() throws SQLException {
        db = this.getWritableDatabase();
    }

    // Close database
    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // User-related methods
    public long addUser(String username, String email, String password) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        return db.insert(TABLE_USERS, null, values);
    }

    public boolean isEmailExists(String email) {
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_EMAIL + " = ?", new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUser(String email, String password) {
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // Expense-related methods
    public long addExpense(String userId, String name, int amount, String category, String notes, String dateTime) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_NOTES, notes);
        values.put(COLUMN_DATE_TIME, dateTime);

        return db.insert(TABLE_EXPENSES, null, values);
    }

    public List<Expense> getAllExpenses(String userId) {
        List<Expense> expenses = new ArrayList<>();
        Cursor cursor = db.query(TABLE_EXPENSES, null,
                COLUMN_USER_ID + " = ?", new String[]{userId},
                null, null, COLUMN_DATE_TIME + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NOTES)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATE_TIME))
                );
                expenses.add(expense);
            }
            cursor.close();
        }
        return expenses;
    }

    public int updateExpense(int expenseId, String name, int amount, String category, String notes, String dateTime) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_NOTES, notes);
        values.put(COLUMN_DATE_TIME, dateTime);

        return db.update(TABLE_EXPENSES, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(expenseId)});
    }

    public void deleteExpense(int expenseId) {
        db.delete(TABLE_EXPENSES, COLUMN_ID + " = ?", new String[]{String.valueOf(expenseId)});
    }

    // Notification-related methods
    public long addNotification(String userId, String actionType, int expenseId, String dateTime, String description) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_ACTION_TYPE, actionType);
        values.put(COLUMN_EXPENSE_ID, expenseId);
        values.put(COLUMN_DATE_TIME, dateTime);
        values.put(COLUMN_DESCRIPTION, description);

        return db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    public List<NotificationRecord> getAllNotifications(String userId) {
        List<NotificationRecord> notifications = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NOTIFICATIONS, null,
                COLUMN_USER_ID + " = ?", new String[]{userId},
                null, null, COLUMN_DATE_TIME + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") NotificationRecord notification = new NotificationRecord(
                        cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_ACTION_TYPE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_EXPENSE_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATE_TIME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION))
                );
                notifications.add(notification);
            }
            cursor.close();
        }
        return notifications;
    }
}
