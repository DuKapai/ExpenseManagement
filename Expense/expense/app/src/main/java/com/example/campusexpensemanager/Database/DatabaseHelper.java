package com.example.campusexpensemanager.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.campusexpensemanager.Entity.Expense;
import com.example.campusexpensemanager.Entity.Noti;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {


    // Database Name and Version
    private static final String DATABASE_NAME = "expense_manager.db";
    private static final int DATABASE_VERSION = 11;
    private static DatabaseHelper instance;

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

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

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
            + COLUMN_TYPE + " VARCHAR(255), "
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
        onCreate(db);
    }

    public void initializeData() {
        new Thread(() -> {
            SQLiteDatabase db = this.getWritableDatabase();
            insertDefaultCategories(db, getDefaultCategories()); // Call here
            db.close();
        }).start();
    }

    // Insert User
    public boolean insertUser(String fullName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USER, null, values);
        db.close();
        return result != -1;
    }

    // Update User
    public boolean updateUser(int userId, String fullName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        int result = db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return result > 0;
    }

    // Delete User
    public boolean deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USER, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
        return result > 0;
    }

    // Hash Password using SHA-256
    public static String hashPassword(String password) {
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

    // Insert Category (with handling for default categories)
    public boolean insertCategory(String name, boolean isDefault) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (isDefault) {  // Đặt email cho category mặc định
            values.put(COLUMN_CATEGORY_EMAIL, "");
        } else {  // Đặt email cho category không mặc định
            values.put(COLUMN_CATEGORY_EMAIL, "");  // Có thể thay bằng email thực tế nếu cần
        }
        values.put(COLUMN_CATEGORY_NAME, name);

        long result = db.insert(TABLE_CATEGORY, null, values);
        db.close();
        return result != -1;
    }

    // Insert Default Categories from a List
    public void insertDefaultCategories(SQLiteDatabase db, List<String> defaultCategories) {
        db.beginTransaction(); // Start transaction for bulk insert
        boolean success = true;

        for (String category : defaultCategories) {
            // Check if the category already exists
            String checkQuery = "SELECT * FROM " + TABLE_CATEGORY + " WHERE " + COLUMN_CATEGORY_NAME + " = ?";
            Cursor cursor = db.rawQuery(checkQuery, new String[]{category});

            if (cursor.getCount() == 0) {  // If not exists, insert
                ContentValues values = new ContentValues();
                values.put(COLUMN_CATEGORY_NAME, category);
                values.put(COLUMN_CATEGORY_EMAIL, "");  // No email for default category
                long result = db.insert(TABLE_CATEGORY, null, values);
                if (result == -1) {
                    success = false;
                    break; // Abort transaction if any insert fails
                }
            }
            cursor.close();
        }

        if (success) {
            db.setTransactionSuccessful(); // Commit transaction if all inserts succeed
        }
        db.endTransaction(); // End transaction
    }

    // Update Category
    public boolean updateCategory(int categoryId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);

        int result = db.update(TABLE_CATEGORY, values, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
        return result > 0;
    }

    // Delete Category
    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_CATEGORY, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
        return result > 0;
    }

    public List<String> getCategoriesByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> categories = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_CATEGORY + " WHERE " + COLUMN_CATEGORY_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
                categories.add(name);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public List<String> getDefaultCategories() {
        List<String> defaultCategories = new ArrayList<>();
        for (String category : DEFAULT_CATEGORIES) {
            defaultCategories.add(category);
        }
        return defaultCategories;
    }

    // Insert Transaction
    public boolean insertTransaction(int id, String email, String name, double amount, String description, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_EMAIL, email);
        values.put(COLUMN_TRANSACTION_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_TYPE, type);

        long result = db.insert(TABLE_TRANSACTION, null, values);
        db.close();
        return result != -1;
    }

    // Update Transaction
    public boolean updateTransaction(int transactionId, String email, String name, double amount, String description, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_EMAIL, email);
        values.put(COLUMN_TRANSACTION_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_TYPE, type);

        int result = db.update(TABLE_TRANSACTION, values, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();
        return result > 0;
    }

    // Delete Transaction
    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TRANSACTION, COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();
        return result > 0;
    }

    // Get Transactions by Email
    public List<Expense> getTransactionsByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Expense> transactions = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + COLUMN_TRANSACTION_EMAIL + " = ?";
            cursor = db.rawQuery(query, new String[]{email});

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSACTION_ID));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSACTION_NAME));
                    @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));
                    @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    @SuppressLint("Range") String createDate = cursor.getString(cursor.getColumnIndex(COLUMN_CREATE_DATE));

                    Expense transaction = new Expense(id, amount, email, name, createDate, description, type);
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error fetching transactions: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return transactions;
    }

    /**
     * Insert a notification into the Notification table.
     *
     * @param title The title of the notification.
     * @param email The email associated with the notification.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public long insertNotification(String title, String email, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_TITLE, title);
        values.put(COLUMN_NOTIFICATION_EMAIL, email);  // Insert the email
        values.put(COLUMN_TYPE, type);  // Insert the email

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
    public List<Noti> getNotificationsByEmail(String email) {
        List<Noti> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Truy vấn lấy dữ liệu
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NOTIFICATION + " WHERE " + COLUMN_NOTIFICATION_EMAIL + " = ?",
                new String[]{email}
        );

        // Duyệt qua kết quả và thêm vào danh sách
        if (cursor.moveToFirst()) {
            do {
                Noti notification = new Noti(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ID)),
                        cursor.getInt(0),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATE_DATE))
                );
                notifications.add(notification);
            } while (cursor.moveToNext());
        }

        // Đóng Cursor và database
        cursor.close();
        db.close();

        return notifications;
    }

}
