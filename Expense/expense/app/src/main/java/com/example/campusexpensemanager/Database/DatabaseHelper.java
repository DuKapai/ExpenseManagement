package com.example.campusexpensemanager.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "expense_manager.db";
    private static final int DATABASE_VERSION = 2;

    // Table User
    public static final String TABLE_USER = "User";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Table Category
    public static final String TABLE_CATEGORY = "Category";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_EMAIL = "email";
    // Default Categories
    private static final String[] DEFAULT_CATEGORIES = {"Family", "Food", "Drink", "Other"};

    // Table Transaction
    public static final String TABLE_TRANSACTION = "Transactions";
    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_TRANSACTION_EMAIL = "email";
    public static final String COLUMN_TRANSACTION_NAME = "name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CREATE_DATE = "create_date";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TYPE = "type";

    // Table Notification
    public static final String TABLE_NOTIFICATION = "Notification";
    public static final String COLUMN_NOTIFICATION_ID = "notification_id";
    public static final String COLUMN_NOTIFICATION_TITLE = "title";
    public static final String COLUMN_NOTIFICATION_EMAIL = "email";

    // Create Table Queries
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " ("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_FULL_NAME + " VARCHAR(255), "
            + COLUMN_EMAIL + " VARCHAR(255) UNIQUE NOT NULL, "
            + COLUMN_PASSWORD + " VARCHAR(255)"
            + ");";

    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + " ("
            + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_CATEGORY_NAME + " VARCHAR(255), "
            + COLUMN_CATEGORY_EMAIL + " VARCHAR(255)"
            + ");";

    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE " + TABLE_TRANSACTION + " ("
            + COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TRANSACTION_EMAIL + " VARCHAR(255), "
            + COLUMN_TRANSACTION_NAME + " VARCHAR(255), "
            + COLUMN_AMOUNT + " DECIMAL(10, 2), "
            + COLUMN_CREATE_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + COLUMN_DESCRIPTION + " VARCHAR(255), "
            + COLUMN_TYPE + " VARCHAR(255)"
            + ");";

    private static final String CREATE_TABLE_NOTIFICATION = "CREATE TABLE " + TABLE_NOTIFICATION + " ("
            + COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NOTIFICATION_TITLE + " VARCHAR(255), "
            + COLUMN_NOTIFICATION_EMAIL + " VARCHAR(255), "
            + COLUMN_CREATE_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_TRANSACTION);
        db.execSQL(CREATE_TABLE_NOTIFICATION);
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
        onCreate(db);
    }

    // Insert User
    public long insertUser(String fullName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password)); // Hash password before saving
        return db.insert(TABLE_USER, null, values);
    }

    // Update User
    public int updateUser(int userId, String fullName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password)); // Hash password before updating
        return db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    // Delete User
    public int deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USER, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    // Hash Password using SHA-256
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("DatabaseHelper", "Error hashing password", e);
            return null;
        }
    }

    // Insert Default Categories
    private void insertDefaultCategories(SQLiteDatabase db) {
        for (String category : DEFAULT_CATEGORIES) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CATEGORY_NAME, category);
            values.put(COLUMN_CATEGORY_EMAIL, "");
            db.insert(TABLE_CATEGORY, null, values);
        }
    }

    // Insert a Category
    public long insertCategory(String name, String email) {
        if (isDefaultCategory(name)) {
            return -1; // Cannot insert default category
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_CATEGORY_EMAIL, email);
        return db.insert(TABLE_CATEGORY, null, values);
    }

    // Update a Category
    public int updateCategory(int categoryId, String name, String email) {
        if (isDefaultCategory(name)) {
            return -1; // Cannot update default category
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_CATEGORY_EMAIL, email);
        return db.update(TABLE_CATEGORY, values, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
    }

    // Delete a Category
    public int deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Prevent deletion of default categories
        Cursor cursor = db.query(TABLE_CATEGORY, new String[]{COLUMN_CATEGORY_NAME},
                COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String categoryName = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
            cursor.close();

            if (isDefaultCategory(categoryName)) {
                return -1; // Cannot delete default category
            }
        }

        return db.delete(TABLE_CATEGORY, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
    }

    // Check if a category is a default category
    private boolean isDefaultCategory(String name) {
        for (String defaultCategory : DEFAULT_CATEGORIES) {
            if (defaultCategory.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // Retrieve all Categories for a specific email
    public Cursor getCategoriesByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CATEGORY + " WHERE " + COLUMN_CATEGORY_EMAIL + " = ?",
                new String[]{email});
    }

    // Insert Transaction
    public long insertTransaction(String email, String name, double amount, String description, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_EMAIL, email);
        values.put(COLUMN_TRANSACTION_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_TYPE, type);

        long result = db.insert(TABLE_TRANSACTION, null, values);
        db.close();
        return result; // Returns the row ID of the inserted row, or -1 if an error occurred
    }

    // Update Transaction
    public int updateTransaction(int transactionId, String email, String name, double amount, String description, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_EMAIL, email);
        values.put(COLUMN_TRANSACTION_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_TYPE, type);

        int result = db.update(TABLE_TRANSACTION, values, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();
        return result; // Returns the number of rows affected
    }

    // Delete Transaction
    public int deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TRANSACTION, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();
        return result; // Returns the number of rows affected
    }

    // Fetch All Transactions (optional helper method)
    public Cursor getAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACTION, null);
    }

    // Retrieve all Transactions for a specific email
    public Cursor getTransactionsByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + COLUMN_TRANSACTION_EMAIL + " = ?",
                new String[]{email});
    }

    /**
     * Insert a notification into the Notification table.
     *
     * @param title The title of the notification.
     * @param email The email associated with the notification.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long insertNotification(String title, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_TITLE, title);
        values.put(COLUMN_NOTIFICATION_EMAIL, email);  // Insert the email

        long result = db.insert(TABLE_NOTIFICATION, null, values);
        db.close();
        return result;
    }

    /**
     * Delete a notification by its ID.
     *
     * @param notificationId The ID of the notification to delete.
     * @return The number of rows affected.
     */
    public int deleteNotification(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NOTIFICATION, COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notificationId)});
        db.close();
        return result;
    }

    /**
     * Retrieve all notifications.
     *
     * @return A Cursor object containing all rows from the Notification table.
     */
    public Cursor getAllNotifications() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATION, null);
    }

    /**
     * Retrieve all notifications for a specific email.
     *
     * @param email The email to filter notifications by.
     * @return A Cursor object containing rows from the Notification table filtered by the email.
     */
    public Cursor getNotificationsByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATION + " WHERE " + COLUMN_NOTIFICATION_EMAIL + " = ?",
                new String[]{email});
    }

}
