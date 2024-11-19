package com.example.campusexpensemanager.Data;

import android.content.Context;

public class DatabaseInitializer {

    private static DatabaseHelper databaseHelper;

    // Initialize the database helper
    public static void initializeDatabase(Context context) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }
    }

    // Get the database helper
    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
