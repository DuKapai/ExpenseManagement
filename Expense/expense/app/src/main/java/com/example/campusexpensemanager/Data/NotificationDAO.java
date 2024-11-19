package com.example.campusexpensemanager.Data;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanager.Notification.NotificationRecord;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public NotificationDAO(Context context) {
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

    // Add a notification
    public void addNotification(NotificationRecord notification) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, notification.getUserId());
        values.put(DatabaseHelper.COLUMN_ACTION_TYPE, notification.getActionType());
        values.put(DatabaseHelper.COLUMN_EXPENSE_ID, notification.getExpenseId());
        values.put(DatabaseHelper.COLUMN_DATE_TIME, notification.getDateTime());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, notification.getDescription());

        db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, values);
    }

    // Get all notifications for a specific user
    public List<NotificationRecord> getAllNotifications(String userId) {
        List<NotificationRecord> notifications = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NOTIFICATIONS, null,
                DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{userId},
                null, null, DatabaseHelper.COLUMN_DATE_TIME + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") NotificationRecord notification = new NotificationRecord(
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ACTION_TYPE)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EXPENSE_ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE_TIME)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION))
                );
                notifications.add(notification);
            }
            cursor.close();
        }
        return notifications;
    }
}
