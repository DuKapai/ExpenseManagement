package com.example.campusexpensemanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private TextView tvRegister;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        tvRegister = findViewById(R.id.tv_register);

        // Redirect to RegisterActivity if user clicks "Register here"
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    public void Login(View view) {
        String emailStr = edtEmail.getText().toString().trim();
        String passwordStr = edtPassword.getText().toString().trim();

        if (emailStr.isEmpty() || passwordStr.isEmpty()) {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog = ProgressDialog.show(this, "Logging in", "Please wait...", true);
            checkLogin(emailStr, passwordStr);
        }
    }

    private void checkLogin(String email, String password) {
        File file = new File(getFilesDir(), "userData.txt");

        if (!file.exists()) {
            progressDialog.dismiss();
            Toast.makeText(this, "No user data found. Please register first.", Toast.LENGTH_SHORT).show();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userData = line.split("\\|");
                if (userData.length == 4 && userData[2].equals(email) && userData[3].equals(password)) {
                    progressDialog.dismiss();

                    // Store login session
                    SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("USER_ID", userData[0]);
                    editor.putString("USER_NAME", userData[1]);
                    editor.putString("USER_EMAIL", userData[2]);
                    editor.apply();

                    // Move to HomeActivity
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
            progressDialog.dismiss();
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Error logging in. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
