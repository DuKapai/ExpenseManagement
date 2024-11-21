package com.example.campusexpensemanager.Verify;

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

import com.example.campusexpensemanager.Data.DatabaseHelper;
import com.example.campusexpensemanager.HomeActivity;
import com.example.campusexpensemanager.R;


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
            return;
        }

        progressDialog = ProgressDialog.show(this, "Logging in", "Please wait...", true);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.open();
        boolean isValidUser = dbHelper.checkUser(emailStr, passwordStr);
        progressDialog.dismiss();

        if (isValidUser) {
            // Store login session
            SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("USER_EMAIL", emailStr);
            editor.apply();

            // Navigate to HomeActivity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
        dbHelper.close();
    }
}
