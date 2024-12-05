package com.example.campusexpensemanager.Database.DAO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.Entity.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryDAO {
    private final SQLiteDatabase db;
    private DatabaseHelper databaseHelper;

    public static final List<String> DEFAULT_CATEGORIES = Arrays.asList("Family", "Food", "Drink", "Other");

    public CategoryDAO(Context context) {
        databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getWritableDatabase();
    }

    public boolean insertCategory(String email, String name) {
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("name", name);
        long result = db.insert("categories", null, values);
        return result != -1;
    }

    public void insertDefaultCategoriesIfNeeded(String email) {
        List<Category> userCategories = getCategoriesByEmail(email);
        List<String> existingCategoryNames = new ArrayList<>();
        for (Category category : userCategories) {
            existingCategoryNames.add(category.getName());
        }

        for (String defaultCategory : DEFAULT_CATEGORIES) {
            if (!existingCategoryNames.contains(defaultCategory)) {
                insertCategory(email, defaultCategory);
            }
        }
    }

    public boolean updateCategory(int id, String email, String name) {
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("name", name);
        int rowsUpdated = db.update("categories", values, "id = ?", new String[]{String.valueOf(id)});
        return rowsUpdated > 0;
    }

    public boolean deleteCategory(int id) {
        int rowsDeleted = db.delete("categories", "id = ?", new String[]{String.valueOf(id)});
        return rowsDeleted > 0;
    }

    public List<Category> getCategoriesByEmail(String email) {
        List<Category> categories = new ArrayList<>();
        //SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_CATEGORY + " WHERE " + DatabaseHelper.COLUMN_CATEGORY_EMAIL + " = ?"; // Changed table name
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_NAME));
                categories.add(new Category(id, name, email));
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();
        return categories;
    }
}
