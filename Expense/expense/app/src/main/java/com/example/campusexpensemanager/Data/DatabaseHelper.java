package com.example.campusexpensemanager.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    // Table names and columns


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create expenses table
        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_AMOUNT + " INTEGER,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_NOTES + " TEXT,"
                + COLUMN_DATE_TIME + " TEXT"
                + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);

        // Create notifications table
        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " TEXT,"
                + COLUMN_ACTION_TYPE + " TEXT,"
                + COLUMN_EXPENSE_ID + " INTEGER,"
                + COLUMN_DATE_TIME + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT"
                + ")";
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);

        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }



    // Method to update an expense
    public int updateExpense(int expenseId, String name, int amount, String category, String notes, String dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_NOTES, notes);
        values.put(COLUMN_DATE_TIME, dateTime);

        return db.update(TABLE_EXPENSES, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(expenseId)});
    }

    // Method to delete an expense
    public void deleteExpense(int expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_ID + " = ?", new String[]{String.valueOf(expenseId)});
        db.close();
    }

    // Method to update a notification
    public int updateNotification(int notificationId, String actionType, int expenseId, String dateTime, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACTION_TYPE, actionType);
        values.put(COLUMN_EXPENSE_ID, expenseId);
        values.put(COLUMN_DATE_TIME, dateTime);
        values.put(COLUMN_DESCRIPTION, description);

        return db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(notificationId)});
    }

    // Method to delete a notification
    public void deleteNotification(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS, COLUMN_ID + " = ?", new String[]{String.valueOf(notificationId)});
        db.close();
    }

    // Add a new user
    public long addUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    // Check if a user exists by email and password
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }


    // Add an expense for a user
    public long addExpense(String userId, String name, int amount, String category, String notes, String dateTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_NOTES, notes);
        values.put(COLUMN_DATE_TIME, dateTime);

        long result = db.insert(TABLE_EXPENSES, null, values);
        db.close();
        return result;
    }

    // Get all expenses for a specific user
    public Cursor getUserExpenses(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EXPENSES,
                null,
                COLUMN_USER_ID + "=?",
                new String[]{userId},
                null, null, COLUMN_DATE_TIME + " DESC");
    }

    // Add a notification record
    public long addNotification(String userId, String actionType, int expenseId, String dateTime, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_ACTION_TYPE, actionType);
        values.put(COLUMN_EXPENSE_ID, expenseId);
        values.put(COLUMN_DATE_TIME, dateTime);
        values.put(COLUMN_DESCRIPTION, description);

        long result = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
        return result;
    }

    // Get all notifications for a specific user
    public Cursor getUserNotifications(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NOTIFICATIONS,
                null,
                COLUMN_USER_ID + "=?",
                new String[]{userId},
                null, null, COLUMN_DATE_TIME + " DESC");
    }
}
