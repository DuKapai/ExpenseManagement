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

import com.example.campusexpensemanager.Database.DAO.UserDAO;
import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.Entity.User;
import com.example.campusexpensemanager.HomeActivity;
import com.example.campusexpensemanager.R;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private TextView tvRegister;
    private UserDAO userDAO;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        tvRegister = findViewById(R.id.tv_register);

        // Redirect to RegisterActivity if user clicks "Register here"
        userDAO = new UserDAO(this);

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
            Toast.makeText(this, "Logging in, Please wait...", Toast.LENGTH_SHORT).show();
            checkLogin(email, password);
        }
    }

    private void checkLogin(String email, String password) {

        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(this);
        }

        List<User> users = userDAO.getUserByEmail(email);

        if (users.isEmpty()) {
            Toast.makeText(this, "No user found with this email", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = users.get(0);

        String hashedPassword = databaseHelper.hashPassword(password);

        if (hashedPassword != null && hashedPassword.equals(user.getPassword())) {
            SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("USER_ID", user.getEmail());
            editor.putString("USER_NAME", user.getFullName());
            editor.putString("USER_EMAIL", user.getEmail());
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }
}
