package com.example.campusexpensemanager.Verify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.HomeActivity;
import com.example.campusexpensemanager.R;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private TextView tvRegister;
    private DatabaseHelper databaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        tvRegister = findViewById(R.id.tv_register);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Redirect to RegisterActivity if user clicks "Register here"
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    public void Login(View view) {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
        } else {
            checkLogin(email, password);
        }
    }

    private void checkLogin(String email, String password) {
        // Query the database for the user with the matching email
        Cursor cursor = null;
        try {
            cursor = databaseHelper.getReadableDatabase().query(
                    DatabaseHelper.TABLE_USER,
                    new String[]{DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_FULL_NAME, DatabaseHelper.COLUMN_EMAIL, DatabaseHelper.COLUMN_PASSWORD},
                    DatabaseHelper.COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String storedPassword = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD));

                // Compare the stored hashed password with the entered password
                if (storedPassword.equals(databaseHelper.hashPassword(password))) {
                    // Password matches
                    @SuppressLint("Range") String userId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
                    @SuppressLint("Range") String fullName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FULL_NAME));
                    @SuppressLint("Range") String userEmail = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL));

                    // Store login session in SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("USER_ID", userId);
                    editor.putString("USER_NAME", fullName);
                    editor.putString("USER_EMAIL", userEmail);
                    editor.apply();

                    // Move to HomeActivity
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Password does not match
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            } else {
                // No user found with the provided email
                Toast.makeText(this, "No user found with this email", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            // Handle database error
            e.printStackTrace();
            Toast.makeText(this, "Database error occurred", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
