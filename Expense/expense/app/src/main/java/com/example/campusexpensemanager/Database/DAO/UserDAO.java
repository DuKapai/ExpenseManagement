package com.example.campusexpensemanager.Database.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.Entity.User;

import java.util.ArrayList;
import java.util.List;


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

    public List<User> getUserByEmail(String email) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM User WHERE email = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") User user = new User(
                        cursor.getInt(cursor.getColumnIndex("user_id")),
                        cursor.getString(cursor.getColumnIndex("full_name")),
                        cursor.getString(cursor.getColumnIndex("email")),
                        cursor.getString(cursor.getColumnIndex("password"))
                );
                users.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    // Delete a user
    public int deleteUser(int userId) {
        return db.delete("User", "user_id = ?", new String[]{String.valueOf(userId)});
    }
}

