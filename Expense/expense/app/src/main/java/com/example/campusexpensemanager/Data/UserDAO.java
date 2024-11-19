package com.example.campusexpensemanager.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;



public class UserDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
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

    // Add a user (using DatabaseHelper's addUser method)
    public long addUser(String fullName, String email, String password) {
        return dbHelper.addUser(fullName, email, password); // Using DatabaseHelper's method
    }

    // Check if the email already exists
    public boolean isEmailExists(String email) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COLUMN_EMAIL + " = ?", new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Save user data and return the result
    public long saveUserData(String fullName, String email, String password) {
        // Check if email already exists in the database
        if (isEmailExists(email)) {
            return -1;  // Return -1 if email already exists
        }

        // Attempt to insert the user into the database
        long result = addUser(fullName, email, password);
        return result;  // Return the result (inserted row ID or -1 if failed)
    }
}
