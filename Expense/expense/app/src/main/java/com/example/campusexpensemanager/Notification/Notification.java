package com.example.campusexpensemanager.Notification;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Notification {
    private final Context context;
    private static final String FILE_NAME = "notificationHistory.txt";

    public Notification(Context context) {
        this.context = context.getApplicationContext();
    }

    public void addRecord(NotificationRecord notification) {
        logNotification(notification);
    }

    // Save notification to file
    private void logNotification(NotificationRecord notification) {
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE | Context.MODE_APPEND)) {
            String notificationString = notification.toString() + "\n";
            fos.write(notificationString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load all notifications from file
    public List<NotificationRecord> loadNotifications() {
        List<NotificationRecord> notifications = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput(FILE_NAME)) {
            Scanner scanner = new Scanner(fis);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                NotificationRecord notification = NotificationRecord.fromString(line);
                notifications.add(notification);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return notifications;
    }
}

