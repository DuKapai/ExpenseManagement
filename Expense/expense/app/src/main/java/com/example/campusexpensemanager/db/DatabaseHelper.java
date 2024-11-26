package com.example.campusexpensemanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "ExpenseManager.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USER = "User";
    private static final String TABLE_CATEGORY = "Category";
    private static final String TABLE_TRANSACTION = "Expense";
    private static final String TABLE_NOTIFICATION = "Notification";

    // Table Create Statements
    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " (" +
            "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "full_name TEXT NOT NULL, " +
            "email TEXT UNIQUE NOT NULL, " +
            "password TEXT NOT NULL, " +
            "create_date DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + " (" +
            "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "img TEXT);";

    private static final String CREATE_TABLE_TRANSACTION = "CREATE TABLE " + TABLE_TRANSACTION + " (" +
            "expense_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "user_id INTEGER NOT NULL, " +
            "name TEXT NOT NULL, " +
            "amount REAL NOT NULL, " +
            "create_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "description TEXT, " +
            "category_id INTEGER, " +
            "FOREIGN KEY (user_id) REFERENCES " + TABLE_USER + "(user_id), " +
            "FOREIGN KEY (category_id) REFERENCES " + TABLE_CATEGORY + "(category_id));";

    private static final String CREATE_TABLE_NOTIFICATION = "CREATE TABLE " + TABLE_NOTIFICATION + " (" +
            "notification_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "create_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "end_date DATETIME, " +
            "category_id INTEGER, " +
            "FOREIGN KEY (category_id) REFERENCES " + TABLE_CATEGORY + "(category_id));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_TRANSACTION);
        db.execSQL(CREATE_TABLE_NOTIFICATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create new tables
        onCreate(db);
    }
}

