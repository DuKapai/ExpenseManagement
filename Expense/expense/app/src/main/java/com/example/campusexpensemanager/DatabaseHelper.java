package com.example.campusexpensemanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "campus_expenses.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_DATE_TIME = "date_time";

    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_ACTION_TYPE = "action_type";
    public static final String COLUMN_EXPENSE_ID = "expense_id";
    public static final String COLUMN_DESCRIPTION = "description";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the expenses table
        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_AMOUNT + " INTEGER,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_NOTES + " TEXT,"
                + COLUMN_DATE_TIME + " TEXT" + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);

        // Create the notifications table
        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " TEXT,"
                + COLUMN_ACTION_TYPE + " TEXT,"
                + COLUMN_EXPENSE_ID + " INTEGER,"
                + COLUMN_DATE_TIME + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT" + ")";
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the tables if they exist and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);
    }
}

