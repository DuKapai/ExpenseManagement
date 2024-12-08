package com.example.campusexpensemanager.Database.DAO;

import android.content.Context;

import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.Entity.Noti;

import java.util.List;

public class NotiDAO {
    private final DatabaseHelper databaseHelper;
    public NotiDAO(Context context){
        this.databaseHelper = new DatabaseHelper(context);
    }

    public void addNoti(String title, String email, String type){
        databaseHelper.insertNotification(title, email, type);
    }
    public List<Noti> getAllNotiByUser(String email){
        return databaseHelper.getNotificationsByEmail(email);
    }

}