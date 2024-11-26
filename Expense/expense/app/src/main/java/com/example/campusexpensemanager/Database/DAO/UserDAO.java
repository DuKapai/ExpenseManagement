package com.example.campusexpensemanager.Database.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanager.Database.DatabaseHelper;


public class UserDAO {
    private final SQLiteDatabase db;

    public UserDAO(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // Insert a new user
    public long insertUser(String fullName, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("email", email);
        values.put("password", password);
        return db.insert("User", null, values);
    }

    // Get a user by ID
    public Cursor getUserById(int userId) {
        return db.rawQuery("SELECT * FROM User WHERE user_id = ?", new String[]{String.valueOf(userId)});
    }

    // Get all users
    public Cursor getAllUsers() {
        return db.rawQuery("SELECT * FROM User", null);
    }

    // Update a user
    public int updateUser(int userId, String fullName, String email, String password) {
        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("email", email);
        values.put("password", password);
        return db.update("User", values, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    // Delete a user
    public int deleteUser(int userId) {
        return db.delete("User", "user_id = ?", new String[]{String.valueOf(userId)});
    }
}

