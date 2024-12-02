package com.example.campusexpensemanager.Verify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusexpensemanager.Database.DAO.UserDAO;
import com.example.campusexpensemanager.Database.DatabaseHelper;
import com.example.campusexpensemanager.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtFullName, edtEmail, edtPassword, edtPassword2;
    private TextView tvLogin;
    private UserDAO userDAO;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        edtFullName = findViewById(R.id.reg_edt_fName);
        edtEmail = findViewById(R.id.reg_edt_email);
        edtPassword = findViewById(R.id.reg_edt_password);
        edtPassword2 = findViewById(R.id.reg_edt_password2);
        tvLogin = findViewById(R.id.tv_login);

        // Initialize DAO and helpers
        userDAO = new UserDAO(this);
        databaseHelper = DatabaseHelper.getInstance(this);

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
        String confirmPassword = edtPassword2.getText().toString().trim();

        if (!validateInput(fullName, email, password, confirmPassword)) {
            return;
        }

        long result = userDAO.insertUser(fullName, email, password);
        if (result != -1) {
            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        } else {
            Toast.makeText(this, "Email đã tồn tại hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validate user input fields.
     *
     * @param fullName       Full name of the user.
     * @param email          Email of the user.
     * @param password       Password of the user.
     * @param confirmPassword Confirm password to ensure passwords match.
     * @return True if all inputs are valid, false otherwise.
     */
    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Validate email format.
     *
     * @param email Email string to validate.
     * @return True if the email is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Navigate to Login Activity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
