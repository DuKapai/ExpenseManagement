package com.example.campusexpensemanager.Database.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.Entity.User;

public class UserDAO {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private SQLiteDatabase getDatabase() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
        }
        return db;
    }

    public long insertUser(String fullName, String email, String password) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("email", email);

        String hashedPassword = DatabaseHelper.hashPassword(password);
        values.put("password", hashedPassword);

        long result = db.insert("User", null, values);
        db.close();
        return result;
    }

    public User checkLogin(String email, String password) {
        SQLiteDatabase db = getDatabase();
        String query = "SELECT * FROM User WHERE email = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String storedPassword = cursor.getString(cursor.getColumnIndex("password"));
            String hashedPassword = DatabaseHelper.hashPassword(password);

            if (hashedPassword != null && hashedPassword.equals(storedPassword)) {
                @SuppressLint("Range") User user = new User(
                        cursor.getInt(cursor.getColumnIndex("user_id")),
                        cursor.getString(cursor.getColumnIndex("full_name")),
                        cursor.getString(cursor.getColumnIndex("email")),
                        storedPassword
                );
                cursor.close();
                return user; // Valid user
            }
        }

        if (cursor != null) cursor.close();
        return null; // Invalid login
    }

    public boolean updateUser(String currentEmail, String newName, String newEmail, String newPassword) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();

        // Update name if provided
        if (!newName.isEmpty()) {
            values.put("full_name", newName);
        }

        // Update email if provided
        if (!newEmail.isEmpty()) {
            values.put("email", newEmail);
        }

        // Update password if provided
        if (!newPassword.isEmpty()) {
            String hashedPassword = DatabaseHelper.hashPassword(newPassword);
            values.put("password", hashedPassword);
        }

        // Perform the update
        int rowsUpdated = db.update("User", values, "email = ?", new String[]{currentEmail});
        db.close();

        // Return success if at least one row was updated
        return rowsUpdated > 0;
    }

}
