package com.example.campusexpensemanager.Models.Verify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtFullName, edtEmail, edtPassword, edtPassword2;
    private TextView tvLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.reg_edt_fName);
        edtEmail = findViewById(R.id.reg_edt_email);
        edtPassword = findViewById(R.id.reg_edt_password);
        edtPassword2 = findViewById(R.id.reg_edt_password2);
        tvLogin = findViewById(R.id.tv_login);

        // Redirect to LoginActivity if user already has an account
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void createAccountBtn(View view) {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String password2 = edtPassword2.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(password2)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save account details to a file
        saveUserData("1", fullName, email, password);
    }

    private void saveUserData(String id, String name, String email, String password) {
        File file = new File(getFilesDir(), "userData.txt");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            String userData = id + "|" + name + "|" + email + "|" + password;
            bw.write(userData);
            bw.newLine();
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();

            // Move to LoginActivity upon successful registration
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close the registration screen

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating account. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
