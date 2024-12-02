package com.example.campusexpensemanager.Verify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.Database.DAO.UserDAO;
import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.Entity.User;
import com.example.campusexpensemanager.HomeActivity;
import com.example.campusexpensemanager.R;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private TextView tvRegister;
    private UserDAO userDAO;
    Button btnLogin;
    private DatabaseHelper databaseHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDAO = new UserDAO(this);
        databaseHelper = DatabaseHelper.getInstance(this);
        databaseHelper.initializeData(); // Call initializeData

        btnLogin = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        tvRegister = findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                } else {
                    checkLogin(email, password);
                }
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void checkLogin(String email, String password) {
        User user = userDAO.checkLogin(email, password);

        if (user == null) {
            Toast.makeText(this, "Email or password incorrect!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save user session
        SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_ID", String.valueOf(user.getUserId()));
        editor.putString("USER_NAME", user.getFullName());
        editor.putString("USER_EMAIL", user.getEmail());
        editor.apply();

        // Navigate to HomeActivity
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
